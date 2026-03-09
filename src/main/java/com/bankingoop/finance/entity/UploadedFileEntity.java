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
 * Supports: audit trail for CSV uploads (interview talking point).
 *
 * Tracks every uploaded CSV with original name, stored path, encryption status,
 * and row count. Useful for re-import and demonstrating data lineage.
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
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
