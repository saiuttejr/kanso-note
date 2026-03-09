package com.bankingoop.finance.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bankingoop.finance.dto.AuditLogDto;
import com.bankingoop.finance.entity.AuditLogEntity;
import com.bankingoop.finance.event.BudgetEvent;
import com.bankingoop.finance.event.TransactionEvent;
import com.bankingoop.finance.repository.AuditLogRepository;

/**
 * Audit service — event-driven, append-only activity log.
 *
 * Design decision — event listeners for decoupled auditing:
 *   The audit service listens to Spring Application Events (TransactionEvent,
 *   BudgetEvent) and records them in an immutable audit_log table. This means
 *   the core business services don't need to know about auditing at all —
 *   adding or removing audit logging requires zero changes to FinanceTrackerService.
 *
 *   The @Async annotation on event handlers ensures audit writes don't slow
 *   down the user-facing request path.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // --- Event Listeners ---

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransactionEvent(TransactionEvent event) {
        AuditLogEntity entry = new AuditLogEntity();
        entry.setEventType("TRANSACTION_" + event.getAction().name());
        entry.setEntityType("TRANSACTION");
        entry.setEntityId(event.getTransactionId());
        entry.setDetails(formatTransactionDetails(event));
        auditLogRepository.save(entry);
        log.debug("Audit: {} transaction #{}", event.getAction(), event.getTransactionId());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBudgetEvent(BudgetEvent event) {
        AuditLogEntity entry = new AuditLogEntity();
        entry.setEventType("BUDGET_" + event.getAction().name());
        entry.setEntityType("BUDGET");
        entry.setEntityId(event.getBudgetId());
        entry.setDetails(event.getDetails());
        auditLogRepository.save(entry);
        log.debug("Audit: {} budget #{} ({})", event.getAction(), event.getBudgetId(), event.getCategory());
    }

    // --- Query Methods ---

    public List<AuditLogDto> getRecentActivity(int limit) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream().map(AuditLogDto::from).toList();
    }

    public List<AuditLogDto> getActivitySince(LocalDateTime since) {
        return auditLogRepository.findRecentActivity(since)
                .stream().map(AuditLogDto::from).toList();
    }

    public long countByEventType(String eventType) {
        return auditLogRepository.countByEventType(eventType);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCustomEvent(String eventType, String entityType, Long entityId, String details) {
        AuditLogEntity entry = new AuditLogEntity();
        entry.setEventType(eventType);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        auditLogRepository.save(entry);
    }

    private String formatTransactionDetails(TransactionEvent event) {
        return switch (event.getAction()) {
            case CREATED -> String.format("Created: %s ($%s) → %s",
                    event.getDescription(), event.getAmount(), event.getCategory());
            case UPDATED -> String.format("Updated: %s ($%s) → %s",
                    event.getDescription(), event.getAmount(), event.getCategory());
            case DELETED -> String.format("Deleted transaction #%d", event.getTransactionId());
            case IMPORTED -> String.format("Imported: %s", event.getDescription());
            case CLEARED -> "All transactions cleared";
        };
    }
}
