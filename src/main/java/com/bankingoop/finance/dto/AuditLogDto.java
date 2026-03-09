package com.bankingoop.finance.dto;

import java.time.LocalDateTime;

import com.bankingoop.finance.entity.AuditLogEntity;

/**
 * Audit log DTO — carries audit trail data to the view/API layer.
 */
public record AuditLogDto(
    Long id,
    String eventType,
    String entityType,
    Long entityId,
    String details,
    LocalDateTime createdAt
) {
    public static AuditLogDto from(AuditLogEntity e) {
        return new AuditLogDto(
            e.getId(),
            e.getEventType(),
            e.getEntityType(),
            e.getEntityId(),
            e.getDetails(),
            e.getCreatedAt()
        );
    }
}
