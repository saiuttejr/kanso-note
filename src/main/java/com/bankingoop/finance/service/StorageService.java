package com.bankingoop.finance.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Supports: offline file storage & optional passphrase encryption (interview talking point).
 *
 * Design decision — interface-based StorageService:
 *   We define storage operations behind an interface so the controller never knows
 *   whether files are encrypted or plain. This keeps the encryption concern isolated
 *   and makes it easy to swap implementations (e.g., for testing with a temp directory).
 *
 * Passphrase tradeoffs (key-management):
 *   - The passphrase is the ONLY key material. It is never stored on disk.
 *   - Losing the passphrase means encrypted data is irrecoverable — there is no
 *     "forgot password" or recovery flow. This is intentional for simplicity.
 *   - We derive the AES key each time via PBKDF2 (expensive but safe).
 *   - For production key-management consider HSM or OS keystore.
 */
public interface StorageService {

    /**
     * Saves an uploaded CSV file to the local uploads directory.
     *
     * @param file       the uploaded multipart file
     * @param passphrase optional — if non-null, the file is AES-encrypted before writing.
     * @return the path where the file was stored
     */
    Path saveCsv(MultipartFile file, char[] passphrase) throws IOException, GeneralSecurityException;

    /**
     * Lists all uploaded files in the uploads directory.
     */
    List<Path> listUploads();

    /**
     * Reads an uploaded file, decrypting if a passphrase is provided.
     *
     * @param fileName   the stored file name
     * @param passphrase optional — required if the file was encrypted
     * @return raw CSV bytes (decrypted if necessary)
     */
    byte[] readUpload(String fileName, char[] passphrase) throws IOException, GeneralSecurityException;

    /**
     * Encrypts the H2 database backup file with the given passphrase.
     * Creates a copy at {@code data/kanso-db-backup.mv.db.enc}.
     *
     * Threat model: protects data-at-rest if the device is lost or stolen.
     * Does NOT protect against an attacker with live OS access.
     *
     * cryptography: suitable for demo/offline use; for production consider HSM or OS keystore.
     */
    void encryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException;

    /**
     * Decrypts a previously encrypted database backup and restores it.
     *
     * WARNING: the passphrase must match the one used during encryption.
     * A wrong passphrase will throw a GeneralSecurityException (AES-GCM tag mismatch).
     * Data encrypted with a forgotten passphrase is irrecoverable — by design.
     */
    void decryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException;
}
