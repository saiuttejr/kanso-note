package com.bankingoop.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.BudgetEntity;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    /** Finds a budget by its category name or empty optional if not found. */
    Optional<BudgetEntity> findByCategory(String category);

    /** Finds all enabled budgets ordered by category name alphabetically. */
    List<BudgetEntity> findByEnabledTrueOrderByCategoryAsc();

    /** Checks if a budget already exists for the given category. */
    boolean existsByCategory(String category);
}
