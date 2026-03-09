package com.bankingoop.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.CategoryRuleEntity;

/**
 * Repository for persistent rule storage and retrieval.
 */
@Repository
public interface CategoryRuleRepository extends JpaRepository<CategoryRuleEntity, Long> {

    /** Finds all enabled rules ordered by priority from highest to lowest. */
    List<CategoryRuleEntity> findByEnabledTrueOrderByPriorityDesc();

    /** Finds user-created rules ordered by creation date (newest first). */
    List<CategoryRuleEntity> findByIsDefaultFalseOrderByCreatedAtDesc();

    /** Finds built-in rules ordered by priority from highest to lowest. */
    List<CategoryRuleEntity> findByIsDefaultTrueOrderByPriorityDesc();
}
