package com.bankingoop.finance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Single-user offline profile with display name and optional encryption salt.
 */
@Entity
@Table(name = "profile")
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName = "Default User";

    @Column(name = "encryption_salt", length = 64)
    private String encryptionSalt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    // Initializes creation and update timestamps on persist.
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    // Updates the timestamp when the profile is modified.
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    // Returns the profile ID.
    public Long getId() { return id; }
    // Sets the profile ID.
    public void setId(Long id) { this.id = id; }

    // Returns the user display name.
    public String getDisplayName() { return displayName; }
    // Sets the display name.
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    // Returns the encryption salt hex string.
    public String getEncryptionSalt() { return encryptionSalt; }
    // Sets the encryption salt.
    public void setEncryptionSalt(String encryptionSalt) { this.encryptionSalt = encryptionSalt; }

    // Returns the creation timestamp.
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Returns the last update timestamp.
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
