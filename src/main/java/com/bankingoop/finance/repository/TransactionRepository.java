package com.bankingoop.finance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.TransactionEntity;

/**
 * Supports: persistent transaction storage in local H2 file database.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findAllByOrderByDateDescIdDesc();

    List<TransactionEntity> findByDateBetweenOrderByDateDescIdDesc(LocalDate start, LocalDate end);

    @Query("SELECT t FROM TransactionEntity t WHERE t.amount < 0 ORDER BY t.date DESC")
    List<TransactionEntity> findAllExpenses();

    @Query("SELECT t FROM TransactionEntity t WHERE t.category = 'Uncategorized' ORDER BY t.description")
    List<TransactionEntity> findUncategorized();

    @Query("SELECT DISTINCT t.description FROM TransactionEntity t WHERE t.category = 'Uncategorized' ORDER BY t.description")
    List<String> findDistinctUncategorizedDescriptions();

    long countByCategory(String category);

    @Query("SELECT t FROM TransactionEntity t WHERE t.date >= :since ORDER BY t.date DESC")
    List<TransactionEntity> findRecent(@Param("since") LocalDate since);
}
