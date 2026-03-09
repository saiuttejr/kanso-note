package com.bankingoop.finance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Audit trail for CSV uploads with file metadata and encryption status.
 */
@Entity
@Table(name = "uploaded_file")
public class UploadedFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(nullable = false)
    private boolean encrypted = false;

    @Column(name = "row_count", nullable = false)
    private int rowCount = 0;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    // Initializes creation timestamp automatically on persist.
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---

    // Returns the file ID.
    public Long getId() { return id; }
    // Sets the file ID.
    public void setId(Long id) { this.id = id; }

    // Returns the original filename.
    public String getOriginalName() { return originalName; }
    // Sets the filename.
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    // Returns the file storage path.
    public String getStoredPath() { return storedPath; }
    // Sets the storage path.
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    // Checks if the file is encrypted.
    public boolean isEncrypted() { return encrypted; }
    // Sets the encryption flag.
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }

    // Returns the number of rows imported from the file.
    public int getRowCount() { return rowCount; }
    // Sets the row count.
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    // Returns the upload timestamp.
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
