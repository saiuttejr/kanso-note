package com.bankingoop.finance.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankingoop.finance.dto.*;
import com.bankingoop.finance.service.AuditService;
import com.bankingoop.finance.service.BudgetService;
import com.bankingoop.finance.service.FinanceTrackerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * RESTful API controller — JSON endpoints for programmatic access.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Kanso Finance API", description = "RESTful API for personal finance management")
// REST API controller providing JSON endpoints for programmatic access to financial data.
public class ApiController {

    private final FinanceTrackerService financeTrackerService;
    private final BudgetService budgetService;
    private final AuditService auditService;

    // Constructor injecting dependent services for API operations.
    public ApiController(FinanceTrackerService financeTrackerService,
                         BudgetService budgetService,
                         AuditService auditService) {
        this.financeTrackerService = financeTrackerService;
        this.budgetService = budgetService;
        this.auditService = auditService;
    }

    // Retrieves all transactions or filters by optional date range.
    @GetMapping("/transactions")
    @Operation(summary = "List transactions", description = "Returns all transactions, optionally filtered by date range")
    public ResponseEntity<List<TransactionDto>> getTransactions(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<TransactionDto> transactions = (from != null || to != null)
                ? financeTrackerService.getTransactions(from, to)
                : financeTrackerService.getTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transactions")
    @Operation(summary = "Create transaction", description = "Add a new transaction with auto-categorization")
    @ApiResponse(responseCode = "201", description = "Transaction created")
    // Creates a new transaction with automatic categorization.
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody CreateTransactionRequest request) {
        TransactionDto dto = financeTrackerService.addManualTransaction(
                request.date(), request.description(), request.amount(), request.category());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Updates an existing transaction by ID.
    // Updates an existing transaction by ID.
    @PutMapping("/transactions/{id}")
    @Operation(summary = "Update transaction")
    public ResponseEntity<TransactionDto> updateTransaction(@PathVariable Long id,
                                                             @RequestBody CreateTransactionRequest request) {
        TransactionDto dto = financeTrackerService.updateTransaction(
                id, request.date(), request.description(), request.amount(), request.category());
        return ResponseEntity.ok(dto);
    }

    // Deletes a transaction by ID.
    // Deletes a transaction by ID.
    @DeleteMapping("/transactions/{id}")
    @Operation(summary = "Delete transaction")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        financeTrackerService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // Returns comprehensive financial analytics including trends, anomalies, and savings rate.
    // Returns comprehensive financial analytics including trends, anomalies, and savings rate.
    @GetMapping("/analytics/summary")
    @Operation(summary = "Get analytics summary", description = "Comprehensive financial analytics including trends, anomalies, and savings rate")
    public ResponseEntity<AnalyticsSummaryDto> getAnalyticsSummary() {
        return ResponseEntity.ok(new AnalyticsSummaryDto(
                financeTrackerService.getTotalIncome(),
                financeTrackerService.getTotalExpense(),
                financeTrackerService.getNetFlow(),
                financeTrackerService.getNetFlow().signum() >= 0,
                financeTrackerService.getSavingsRatePercent(),
                financeTrackerService.getSavingsRateLevel(),
                financeTrackerService.getTopCategories(5),
                financeTrackerService.getMonthlyTrends(),
                financeTrackerService.detectRecurringTransactions()
        ));
    }

    // Provides monthly income/expense trends with deltas and rolling averages.
    // Provides monthly income/expense trends with deltas and rolling averages.
    @GetMapping("/analytics/monthly-trends")
    @Operation(summary = "Get monthly trends", description = "Monthly income/expense with month-over-month delta and 3-month rolling average")
    public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrends() {
        return ResponseEntity.ok(financeTrackerService.getMonthlyTrends());
    }

    // Detects unusual transactions using statistical analysis (mean + 2σ threshold).
    // Detects unusual transactions using statistical analysis (mean + 2σ threshold).
    @GetMapping("/analytics/anomalies")
    @Operation(summary = "Detect unusual transactions", description = "Statistical anomaly detection using mean + 2σ threshold")
    public ResponseEntity<List<UnusualTransactionDto>> getAnomalies() {
        return ResponseEntity.ok(financeTrackerService.detectUnusualTransactions());
    }

    // Identifies recurring transactions by description and amount similarity.
    // Identifies recurring transactions by description and amount similarity.
    @GetMapping("/analytics/recurring")
    @Operation(summary = "Detect recurring transactions", description = "Identifies recurring transactions by description and amount similarity")
    public ResponseEntity<List<RecurringTransactionDto>> getRecurring() {
        return ResponseEntity.ok(financeTrackerService.detectRecurringTransactions());
    }

    // Returns top spending categories with configurable limit.
    // Returns top spending categories with configurable limit.
    @GetMapping("/analytics/categories")
    @Operation(summary = "Top spending categories")
    public ResponseEntity<List<CategorySpendDto>> getTopCategories(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(financeTrackerService.getTopCategories(limit));
    }

    // Returns all enabled budgets.
    @GetMapping("/budgets")
    @Operation(summary = "List budgets", description = "Returns all enabled budgets")
    public ResponseEntity<List<BudgetDto>> getBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    // Provides real-time budget status with spending analysis against limits.
    // Provides real-time budget status with spending analysis against limits.
    @GetMapping("/budgets/status")
    @Operation(summary = "Get budget statuses", description = "Real-time budget status with spending analysis against limits")
    public ResponseEntity<List<BudgetStatusDto>> getBudgetStatuses() {
        return ResponseEntity.ok(budgetService.getBudgetStatuses());
    }

    // Creates a new budget with monthly limit and alert threshold.
    // Creates a new budget with monthly limit and alert threshold.
    @PostMapping("/budgets")
    @Operation(summary = "Create budget")
    @ApiResponse(responseCode = "201", description = "Budget created")
    public ResponseEntity<BudgetDto> createBudget(@RequestBody CreateBudgetRequest request) {
        BudgetDto dto = budgetService.createBudget(
                request.category(), request.monthlyLimit(), request.alertThreshold());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Updates an existing budget by ID with new limits.
    // Updates an existing budget by ID with new limits.
    @PutMapping("/budgets/{id}")
    @Operation(summary = "Update budget")
    public ResponseEntity<BudgetDto> updateBudget(@PathVariable Long id,
                                                    @RequestBody UpdateBudgetRequest request) {
        BudgetDto dto = budgetService.updateBudget(id, request.monthlyLimit(), request.alertThreshold());
        return ResponseEntity.ok(dto);
    }

    // Deletes a budget by ID.
    // Deletes a budget by ID.
    @DeleteMapping("/budgets/{id}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    // Lists all custom and default categorization rules.
    // Lists all custom and default categorization rules.
    @GetMapping("/rules")
    @Operation(summary = "List categorization rules")
    public ResponseEntity<Map<String, List<CategoryRuleDto>>> getRules() {
        return ResponseEntity.ok(Map.of(
                "custom", financeTrackerService.getCustomRules(),
                "default", financeTrackerService.getDefaultRules()
        ));
    }

    // Generates auto-suggested rules based on uncategorized transactions.
    // Generates auto-suggested rules based on uncategorized transactions.
    @GetMapping("/rules/suggestions")
    @Operation(summary = "Get rule suggestions", description = "Auto-suggested rules based on uncategorized transactions")
    public ResponseEntity<List<RuleSuggestionDto>> getRuleSuggestions() {
        return ResponseEntity.ok(financeTrackerService.suggestRules());
    }

    // Returns the most recent audit log entries with configurable limit.
    // Returns the most recent audit log entries with configurable limit.
    @GetMapping("/unusual")
    @Operation(summary = "Recent activity", description = "Returns the most recent audit log entries")
    public ResponseEntity<List<AuditLogDto>> getRecentActivity(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getRecentActivity(limit));
    }

    public record CreateTransactionRequest(
            LocalDate date, String description, BigDecimal amount, String category) {}

    public record CreateBudgetRequest(
            String category, BigDecimal monthlyLimit, BigDecimal alertThreshold) {}

    public record UpdateBudgetRequest(
            BigDecimal monthlyLimit, BigDecimal alertThreshold) {}
}
