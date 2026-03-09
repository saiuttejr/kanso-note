package com.bankingoop.finance.service;

import com.bankingoop.finance.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Offline file persistence with optional AES-256 encryption.
 */
@Service
public class StorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path uploadDir;
    private final Path dbPath;

    /** Initializes storage service with configured upload and database directory paths. */
    public StorageServiceImpl(
            @Value("${kanso.storage.upload-dir:./data/uploads}") String uploadDir,
            @Value("${kanso.storage.db-path:./data/kanso-db}") String dbPath) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.dbPath = Paths.get(dbPath).toAbsolutePath().normalize();
        ensureDirectoryExists(this.uploadDir);
        log.info("StorageService initialised — uploads: {}, db: {}", this.uploadDir, this.dbPath);
    }

    /** Saves CSV with timestamp prefix and optional encryption to uploads directory. */
    @Override
    public Path saveCsv(MultipartFile file, char[] passphrase) throws IOException, GeneralSecurityException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(FILE_TS);
        boolean encrypt = passphrase != null && passphrase.length > 0;
        String storedName = timestamp + "_" + originalName + (encrypt ? ".enc" : "");

        Path target = uploadDir.resolve(storedName).normalize();
        // Path traversal guard: ensure the resolved path is still inside uploadDir
        if (!target.startsWith(uploadDir)) {
            throw new SecurityException("Invalid file path — possible path traversal attempt.");
        }

        if (encrypt) {
            // Read → encrypt → write (all in memory; acceptable for ≤5 MB files)
            try (InputStream in = file.getInputStream();
                 OutputStream out = Files.newOutputStream(target)) {
                EncryptionUtil.encryptStream(in, out, passphrase);
            }
            log.info("Saved encrypted CSV upload: {}", storedName);
        } else {
            file.transferTo(target.toFile());
            log.info("Saved plain CSV upload: {}", storedName);
        }

        return target;
    }

    /** Lists all regular files in uploads directory in sorted order. */
    @Override
    public List<Path> listUploads() {
        if (!Files.isDirectory(uploadDir)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(uploadDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.error("Failed to list uploads directory", e);
            return Collections.emptyList();
        }
    }

    /** Reads file with path traversal guard and optional decryption. */
    @Override
    public byte[] readUpload(String fileName, char[] passphrase) throws IOException, GeneralSecurityException {
        Path filePath = uploadDir.resolve(fileName).normalize();
        // Path traversal guard
        if (!filePath.startsWith(uploadDir)) {
            throw new SecurityException("Invalid file path.");
        }
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileName);
        }

        byte[] raw = Files.readAllBytes(filePath);

        if (fileName.endsWith(".enc")) {
            if (passphrase == null || passphrase.length == 0) {
                throw new IllegalArgumentException("Passphrase required to read encrypted file.");
            }
            log.debug("Decrypting upload: {}", fileName);
            return EncryptionUtil.decrypt(raw, passphrase);
        }

        return raw;
    }

    /** Reads H2 database file and writes encryption using EncryptionUtil. */
    @Override
    public void encryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException {
        if (passphrase == null || passphrase.length == 0) {
            throw new IllegalArgumentException("Passphrase is required for encryption.");
        }

        Path dbFile = resolveDbFile();
        if (!Files.exists(dbFile)) {
            throw new IOException("Database file not found: " + dbFile);
        }

        Path encFile = dbFile.resolveSibling(dbFile.getFileName() + ".enc");
        byte[] plain = Files.readAllBytes(dbFile);
        byte[] encrypted = EncryptionUtil.encrypt(plain, passphrase);
        Files.write(encFile, encrypted);

        log.info("Database backup encrypted → {}", encFile.getFileName());
    }

    /** Decrypts encrypted backup and writes to restored file with .mv.db extension. */
    @Override
    public void decryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException {
        if (passphrase == null || passphrase.length == 0) {
            throw new IllegalArgumentException("Passphrase is required for decryption.");
        }

        Path dbFile = resolveDbFile();
        Path encFile = dbFile.resolveSibling(dbFile.getFileName() + ".enc");
        if (!Files.exists(encFile)) {
            throw new IOException("Encrypted database backup not found: " + encFile);
        }

        byte[] encrypted = Files.readAllBytes(encFile);
        byte[] decrypted = EncryptionUtil.decrypt(encrypted, passphrase);

        Path restoredFile = dbFile.resolveSibling(dbFile.getFileName() + ".restored.mv.db");
        Files.write(restoredFile, decrypted);

        log.info("Database backup decrypted → {}", restoredFile.getFileName());
    }

    /** Resolves H2 database file path handling both .mv.db extension and raw paths. */
    private Path resolveDbFile() {
        // H2 appends .mv.db to the configured path
        Path mvDb = Paths.get(dbPath + ".mv.db");
        if (Files.exists(mvDb)) {
            return mvDb;
        }
        // Fallback: maybe the path already includes the extension
        return dbPath;
    }

    /** Removes directory components and replaces unsafe characters in file names. */
    private String sanitizeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "upload.csv";
        }
        // Strip any directory components
        String name = Paths.get(originalName).getFileName().toString();
        // Replace characters that could cause issues on various OS file systems
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /** Creates directory recursively and throws runtime exception on failure. */
    private void ensureDirectoryExists(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("Failed to create directory: {}", dir, e);
            throw new RuntimeException("Cannot create storage directory: " + dir, e);
        }
    }
}
