package com.bankingoop.finance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.TransactionEntity;

/**
 * Repository for transaction persistence in local H2 database.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    /** Finds all transactions ordered by date (newest first) then by ID. */
    List<TransactionEntity> findAllByOrderByDateDescIdDesc();

    /** Finds transactions within date range ordered by date descending then ID. */
    List<TransactionEntity> findByDateBetweenOrderByDateDescIdDesc(LocalDate start, LocalDate end);

    /** Finds all negative (expense) transactions ordered by date descending. */
    @Query("SELECT t FROM TransactionEntity t WHERE t.amount < 0 ORDER BY t.date DESC")
    List<TransactionEntity> findAllExpenses();

    /** Finds all transactions with 'Uncategorized' category ordered by description. */
    @Query("SELECT t FROM TransactionEntity t WHERE t.category = 'Uncategorized' ORDER BY t.description")
    List<TransactionEntity> findUncategorized();

    /** Finds unique descriptions of uncategorized transactions for rule suggestions. */
    @Query("SELECT DISTINCT t.description FROM TransactionEntity t WHERE t.category = 'Uncategorized' ORDER BY t.description")
    List<String> findDistinctUncategorizedDescriptions();

    /** Returns count of transactions in a specific category. */
    long countByCategory(String category);

    /** Finds transactions since specified date ordered by date descending. */
    @Query("SELECT t FROM TransactionEntity t WHERE t.date >= :since ORDER BY t.date DESC")
    List<TransactionEntity> findRecent(@Param("since") LocalDate since);
}
