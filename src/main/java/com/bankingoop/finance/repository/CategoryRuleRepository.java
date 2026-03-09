package com.bankingoop.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.CategoryRuleEntity;

/**
 * Supports: persistent rule storage so custom rules survive restarts.
 */
@Repository
public interface CategoryRuleRepository extends JpaRepository<CategoryRuleEntity, Long> {

    List<CategoryRuleEntity> findByEnabledTrueOrderByPriorityDesc();

    List<CategoryRuleEntity> findByIsDefaultFalseOrderByCreatedAtDesc();

    List<CategoryRuleEntity> findByIsDefaultTrueOrderByPriorityDesc();
}
