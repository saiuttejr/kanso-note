package com.bankingoop.finance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Supports: offline passphrase-based encryption (interview talking point).
 *
 * Design decision — PBKDF2 + AES-256-GCM:
 *   We derive a 256-bit key from the user's passphrase using PBKDF2WithHmacSHA256
 *   (600 000 iterations as of OWASP 2023 guidance) so the key never leaves RAM and
 *   never touches the network. AES-GCM provides authenticated encryption (integrity +
 *   confidentiality). A random 16-byte salt and 12-byte IV are prepended to every
 *   ciphertext so each encryption produces unique output even for identical input.
 *
 * Threat model:
 *   - Protects data-at-rest if the device is lost or the DB file is copied.
 *   - Does NOT protect against a running attacker with full OS access (they can read
 *     heap memory while the app has the key loaded).
 *   - The passphrase is the single point of trust: losing it means data is
 *     irrecoverable. There is no "forgot password" flow — by design, for simplicity.
 *
 * Key-management tradeoff:
 *   We deliberately avoid storing the derived key anywhere. The user must supply the
 *   passphrase every time encryption/decryption is needed. This is the simplest
 *   model for a single-user offline app and avoids the complexity of OS keystore
 *   integration. For production key-management consider HSM or OS keystore.
 *
 * cryptography: this is suitable for demo/offline use; for production key-management
 * consider HSM or OS keystore.
 */
public final class EncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    // OWASP 2023 recommends >= 600 000 iterations for PBKDF2-HMAC-SHA256
    private static final int PBKDF2_ITERATIONS = 600_000;

    private EncryptionUtil() {
        // Utility class — no instantiation
    }

    // --- Public API ---

    /**
     * Derives a 256-bit AES key from a passphrase and salt using PBKDF2.
     * The high iteration count makes brute-force attacks expensive.
     */
    public static SecretKey deriveKey(char[] passphrase, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } finally {
            spec.clearPassword(); // minimise passphrase lifetime in memory
        }
    }

    /**
     * Encrypts {@code plainBytes} with AES-256-GCM.
     * Output format: [16-byte salt][12-byte IV][ciphertext+tag]
     */
    public static byte[] encrypt(byte[] plainBytes, char[] passphrase) throws GeneralSecurityException {
        byte[] salt = randomBytes(SALT_LENGTH_BYTES);
        byte[] iv = randomBytes(IV_LENGTH_BYTES);
        SecretKey key = deriveKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] cipherText = cipher.doFinal(plainBytes);

        // Prepend salt + IV so decrypt can reconstruct the same key and IV
        byte[] output = new byte[salt.length + iv.length + cipherText.length];
        System.arraycopy(salt, 0, output, 0, salt.length);
        System.arraycopy(iv, 0, output, salt.length, iv.length);
        System.arraycopy(cipherText, 0, output, salt.length + iv.length, cipherText.length);

        log.debug("Encrypted {} bytes → {} bytes (salt+IV+ciphertext)", plainBytes.length, output.length);
        return output;
    }

    /**
     * Decrypts data produced by {@link #encrypt(byte[], char[])}.
     * Reads the prepended salt and IV, re-derives the key, then decrypts.
     */
    public static byte[] decrypt(byte[] encryptedBytes, char[] passphrase) throws GeneralSecurityException {
        if (encryptedBytes.length < SALT_LENGTH_BYTES + IV_LENGTH_BYTES) {
            throw new IllegalArgumentException("Encrypted data too short — possibly corrupted.");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        byte[] iv = new byte[IV_LENGTH_BYTES];
        System.arraycopy(encryptedBytes, 0, salt, 0, SALT_LENGTH_BYTES);
        System.arraycopy(encryptedBytes, SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES);

        byte[] cipherText = new byte[encryptedBytes.length - SALT_LENGTH_BYTES - IV_LENGTH_BYTES];
        System.arraycopy(encryptedBytes, SALT_LENGTH_BYTES + IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

        SecretKey key = deriveKey(passphrase, salt);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

        log.debug("Decrypting {} bytes of ciphertext", cipherText.length);
        return cipher.doFinal(cipherText);
    }

    /**
     * Encrypts a file on disk in-place (reads → encrypts → overwrites).
     * Used by StorageService for CSV uploads and DB backup encryption.
     */
    public static void encryptFile(Path filePath, char[] passphrase) throws GeneralSecurityException, IOException {
        byte[] plainBytes = Files.readAllBytes(filePath);
        byte[] encrypted = encrypt(plainBytes, passphrase);
        Files.write(filePath, encrypted);
        log.info("Encrypted file: {}", filePath.getFileName());
    }

    /**
     * Decrypts a file on disk in-place (reads → decrypts → overwrites).
     */
    public static void decryptFile(Path filePath, char[] passphrase) throws GeneralSecurityException, IOException {
        byte[] encryptedBytes = Files.readAllBytes(filePath);
        byte[] decrypted = decrypt(encryptedBytes, passphrase);
        Files.write(filePath, decrypted);
        log.info("Decrypted file: {}", filePath.getFileName());
    }

    /**
     * Convenience: encrypt an InputStream and write the ciphertext to an OutputStream.
     * Reads the full stream into memory — acceptable for CSV files under the 5 MB upload limit.
     */
    public static void encryptStream(InputStream in, OutputStream out, char[] passphrase)
            throws GeneralSecurityException, IOException {
        byte[] plainBytes = in.readAllBytes();
        byte[] encrypted = encrypt(plainBytes, passphrase);
        out.write(encrypted);
    }

    /**
     * Generates a random hex-encoded salt string (for storing in the profile table).
     */
    public static String generateSaltHex() {
        byte[] salt = randomBytes(SALT_LENGTH_BYTES);
        return bytesToHex(salt);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // --- Internal helpers ---

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
