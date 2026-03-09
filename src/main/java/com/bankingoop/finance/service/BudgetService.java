package com.bankingoop.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingoop.finance.dto.BudgetDto;
import com.bankingoop.finance.dto.BudgetStatusDto;
import com.bankingoop.finance.entity.BudgetEntity;
import com.bankingoop.finance.entity.TransactionEntity;
import com.bankingoop.finance.event.BudgetEvent;
import com.bankingoop.finance.exception.DuplicateResourceException;
import com.bankingoop.finance.exception.ResourceNotFoundException;
import com.bankingoop.finance.repository.BudgetRepository;
import com.bankingoop.finance.repository.TransactionRepository;

/**
 * Budget management service — per-category spending limits with real-time monitoring.
 *
 * Design decision — two-tier alerting:
 *   Each budget has a configurable alert threshold (default 80%). When current-month
 *   spending crosses the threshold, the status becomes "warning". When it exceeds
 *   the monthly limit, the status becomes "exceeded". This gives users early warning
 *   before actually going over budget.
 *
 *   Budget checks are triggered reactively via Spring Events when transactions are
 *   created/updated, keeping the budget logic decoupled from the core transaction flow.
 */
@Service
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BudgetService(BudgetRepository budgetRepository,
                         TransactionRepository transactionRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BudgetDto createBudget(String category, BigDecimal monthlyLimit, BigDecimal alertThreshold) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly limit must be greater than zero.");
        }
        if (budgetRepository.existsByCategory(category.trim())) {
            throw new DuplicateResourceException("Budget", category.trim());
        }

        BudgetEntity entity = new BudgetEntity();
        entity.setCategory(category.trim());
        entity.setMonthlyLimit(monthlyLimit.setScale(2, RoundingMode.HALF_UP));
        entity.setAlertThreshold(alertThreshold != null ? alertThreshold : new BigDecimal("80.00"));
        entity.setEnabled(true);

        BudgetEntity saved = budgetRepository.save(entity);
        log.info("Created budget for '{}': ${} limit", category, monthlyLimit);

        eventPublisher.publishEvent(new BudgetEvent(this, BudgetEvent.Action.CREATED,
                saved.getId(), saved.getCategory(), "Budget created with limit $" + monthlyLimit));

        return BudgetDto.from(saved);
    }

    @Transactional
    public BudgetDto updateBudget(Long id, BigDecimal monthlyLimit, BigDecimal alertThreshold) {
        BudgetEntity entity = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        if (monthlyLimit != null) entity.setMonthlyLimit(monthlyLimit.setScale(2, RoundingMode.HALF_UP));
        if (alertThreshold != null) entity.setAlertThreshold(alertThreshold);

        BudgetEntity saved = budgetRepository.save(entity);
        log.info("Updated budget #{} for '{}'", id, entity.getCategory());

        eventPublisher.publishEvent(new BudgetEvent(this, BudgetEvent.Action.UPDATED,
                saved.getId(), saved.getCategory(), "Budget updated"));

        return BudgetDto.from(saved);
    }

    @Transactional
    public void deleteBudget(Long id) {
        BudgetEntity entity = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        budgetRepository.delete(entity);
        log.info("Deleted budget #{} for '{}'", id, entity.getCategory());

        eventPublisher.publishEvent(new BudgetEvent(this, BudgetEvent.Action.DELETED,
                id, entity.getCategory(), "Budget deleted"));
    }

    public List<BudgetDto> getAllBudgets() {
        return budgetRepository.findByEnabledTrueOrderByCategoryAsc()
                .stream().map(BudgetDto::from).toList();
    }

    /**
     * Computes real-time budget status for all enabled budgets against current month spending.
     * This is the core analytics feature — combining budget limits with actual transaction data.
     */
    public List<BudgetStatusDto> getBudgetStatuses() {
        List<BudgetEntity> budgets = budgetRepository.findByEnabledTrueOrderByCategoryAsc();
        if (budgets.isEmpty()) return List.of();

        Map<String, BigDecimal> currentSpend = getCurrentMonthSpendByCategory();
        List<BudgetStatusDto> statuses = new ArrayList<>();

        for (BudgetEntity budget : budgets) {
            BigDecimal spent = currentSpend.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
            BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
            BigDecimal utilization = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String alertLevel = "safe";
            if (utilization.compareTo(BigDecimal.valueOf(100)) >= 0) {
                alertLevel = "exceeded";
            } else if (utilization.compareTo(budget.getAlertThreshold()) >= 0) {
                alertLevel = "warning";
            }

            statuses.add(new BudgetStatusDto(
                    budget.getId(), budget.getCategory(), budget.getMonthlyLimit(),
                    spent, remaining, utilization, alertLevel));
        }

        return statuses;
    }

    /**
     * Check budget after a transaction and publish events if thresholds are crossed.
     */
    public void checkBudgetForCategory(String category) {
        budgetRepository.findByCategory(category).ifPresent(budget -> {
            if (!budget.isEnabled()) return;

            Map<String, BigDecimal> currentSpend = getCurrentMonthSpendByCategory();
            BigDecimal spent = currentSpend.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal utilization = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            if (utilization.compareTo(BigDecimal.valueOf(100)) >= 0) {
                log.warn("BUDGET EXCEEDED: '{}' — spent ${} of ${} limit",
                        category, spent, budget.getMonthlyLimit());
                eventPublisher.publishEvent(new BudgetEvent(this, BudgetEvent.Action.EXCEEDED,
                        budget.getId(), category, String.format("Spent $%s of $%s limit (%.1f%%)",
                                spent, budget.getMonthlyLimit(), utilization)));
            } else if (utilization.compareTo(budget.getAlertThreshold()) >= 0) {
                log.info("BUDGET WARNING: '{}' — spent ${} of ${} limit ({}%)",
                        category, spent, budget.getMonthlyLimit(), utilization);
                eventPublisher.publishEvent(new BudgetEvent(this, BudgetEvent.Action.THRESHOLD_REACHED,
                        budget.getId(), category, String.format("Spending at %.1f%% of limit", utilization)));
            }
        });
    }

    private Map<String, BigDecimal> getCurrentMonthSpendByCategory() {
        YearMonth currentMonth = YearMonth.now();
        return transactionRepository.findAll().stream()
                .filter(TransactionEntity::isExpense)
                .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
                .collect(Collectors.groupingBy(
                        TransactionEntity::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)));
    }
}
