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

    List<AuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<AuditLogEntity> findByEventTypeOrderByCreatedAtDesc(String eventType);

    @Query("SELECT a FROM AuditLogEntity a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLogEntity> findRecentActivity(@Param("since") LocalDateTime since);

    long countByEventType(String eventType);
}
