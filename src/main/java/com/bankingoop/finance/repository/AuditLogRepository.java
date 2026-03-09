package com.bankingoop.finance.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.AuditLogEntity;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    /** Finds paginated audit log entries ordered by creation timestamp (newest first). */
    List<AuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Finds all audit log entries of a specific type ordered by creation time. */
    List<AuditLogEntity> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /** Finds audit log entries recorded since specified timestamp ordered by creation time. */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLogEntity> findRecentActivity(@Param("since") LocalDateTime since);

    /** Returns count of audit log entries for a given event type. */
    long countByEventType(String eventType);
}
