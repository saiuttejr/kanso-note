package com.bankingoop.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.BudgetEntity;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    Optional<BudgetEntity> findByCategory(String category);

    List<BudgetEntity> findByEnabledTrueOrderByCategoryAsc();

    boolean existsByCategory(String category);
}
