package com.bankingoop.finance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for offline file storage and directory initialization.
 */
@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${kanso.storage.upload-dir:./data/uploads}")
    private String uploadDir;

    @Value("${kanso.storage.db-path:./data/kanso-db}")
    private String dbPath;

    /** Post-construct initialization creating upload and database directories if missing. */
    @PostConstruct
    public void initDirectories() {
        createDir(Paths.get(uploadDir));
        createDir(Paths.get(dbPath).getParent());
        log.info("Local storage directories verified — uploads: {}, db parent: {}",
                uploadDir, Paths.get(dbPath).getParent());
    }

    /** Recursively creates specified directory, throwing runtime exception on failure. */
    private void createDir(Path dir) {
        if (dir == null) {
            return;
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("Failed to create directory: {}", dir, e);
            throw new RuntimeException("Cannot initialise storage directory: " + dir, e);
        }
    }
}
