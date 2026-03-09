package com.bankingoop.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bankingoop.finance.dto.BudgetStatusDto;
import com.bankingoop.finance.dto.UnusualTransactionDto;

/**
 * Scheduled analytics service — runs periodic background tasks.
 *
 * Design decision — @Scheduled for proactive monitoring:
 *   Rather than computing analytics only on dashboard load, this service
 *   runs periodic checks to proactively identify budget overruns and
 *   spending anomalies. This demonstrates familiarity with Spring's
 *   task scheduling infrastructure and background processing patterns.
 */
@Service
public class ScheduledAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledAnalyticsService.class);

    private final FinanceTrackerService financeTrackerService;
    private final BudgetService budgetService;
    private final AuditService auditService;

    public ScheduledAnalyticsService(FinanceTrackerService financeTrackerService,
                                     BudgetService budgetService,
                                     AuditService auditService) {
        this.financeTrackerService = financeTrackerService;
        this.budgetService = budgetService;
        this.auditService = auditService;
    }

    /**
     * Runs every hour — checks all budgets for threshold violations.
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    public void checkBudgetAlerts() {
        log.debug("Running scheduled budget check...");
        List<BudgetStatusDto> statuses = budgetService.getBudgetStatuses();

        long exceeded = statuses.stream().filter(BudgetStatusDto::isExceeded).count();
        long warnings = statuses.stream().filter(BudgetStatusDto::isWarning).count();

        if (exceeded > 0 || warnings > 0) {
            log.info("Budget check: {} exceeded, {} warning out of {} budgets",
                    exceeded, warnings, statuses.size());
        }
    }

    /**
     * Runs daily at midnight — generates a daily spending summary audit entry.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailySpendingSummary() {
        log.debug("Generating daily spending summary...");
        BigDecimal income = financeTrackerService.getTotalIncome();
        BigDecimal expense = financeTrackerService.getTotalExpense();
        int count = financeTrackerService.getTransactionCount();

        String summary = String.format("Daily summary: %d transactions, $%s income, $%s expenses, $%s net",
                count, income, expense, income.subtract(expense));

        auditService.logCustomEvent("DAILY_SUMMARY", "SYSTEM", null, summary);
        log.info(summary);
    }

    /**
     * Runs every 6 hours — detects new spending anomalies.
     */
    @Scheduled(fixedRate = 21600000, initialDelay = 120000)
    public void detectAnomalies() {
        log.debug("Running scheduled anomaly detection...");
        List<UnusualTransactionDto> anomalies = financeTrackerService.detectUnusualTransactions();

        if (!anomalies.isEmpty()) {
            log.info("Anomaly detection found {} unusual transaction(s)", anomalies.size());
        }
    }
}
