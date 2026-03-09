package com.bankingoop.finance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Immutable audit log entry recording all significant user actions.
 */
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_log_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_log_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_log_entity", columnList = "entity_type, entity_id")
})
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 1000)
    private String details;

    @Column(length = 2000)
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    // Initializes creation timestamp automatically on persist.
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- Getters / Setters ---

    // Returns the audit log ID.
    public Long getId() { return id; }

    // Returns the event type (CREATE, UPDATE, DELETE, etc).
    public String getEventType() { return eventType; }
    // Sets the event type.
    public void setEventType(String eventType) { this.eventType = eventType; }

    // Returns the entity type being audited.
    public String getEntityType() { return entityType; }
    // Sets the entity type.
    public void setEntityType(String entityType) { this.entityType = entityType; }

    // Returns the ID of the audited entity.
    public Long getEntityId() { return entityId; }
    // Sets the entity ID.
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    // Returns the event details description.
    public String getDetails() { return details; }
    // Sets the details.
    public void setDetails(String details) { this.details = details; }

    // Returns the event metadata as JSON.
    public String getMetadata() { return metadata; }
    // Sets the metadata.
    public void setMetadata(String metadata) { this.metadata = metadata; }

    // Returns the creation timestamp.
    public LocalDateTime getCreatedAt() { return createdAt; }
}
