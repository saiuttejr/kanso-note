package com.bankingoop.finance.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Service for offline file storage and optional passphrase encryption.
 */
public interface StorageService {

    /** Saves uploaded CSV file with optional AES-256 encryption to local storage. */
    Path saveCsv(MultipartFile file, char[] passphrase) throws IOException, GeneralSecurityException;

    /** Saves CSV from byte array with optional AES-256 encryption to local storage. */
    Path saveCsvFromBytes(byte[] fileBytes, String originalFilename, char[] passphrase) throws IOException, GeneralSecurityException;

    /** Lists all files in the uploads directory in sorted order. */
    List<Path> listUploads();

    /** Reads an uploaded file and decrypts if passphrase provided. */
    byte[] readUpload(String fileName, char[] passphrase) throws IOException, GeneralSecurityException;

    /** Encrypts H2 database backup with AES-256-GCM for offline protection. */
    void encryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException;

    /** Decrypts and restores encrypted database backup using matching passphrase. */
    void decryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException;
}
