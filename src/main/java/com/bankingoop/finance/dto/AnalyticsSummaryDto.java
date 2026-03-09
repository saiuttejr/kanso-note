package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Supports: analytics dashboard — savings rate, top categories, and summary metrics.
 */
public record AnalyticsSummaryDto(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal netFlow,
    boolean netPositive,
    BigDecimal savingsRatePercent,
    String savingsRateLevel,           // "good", "warning", "danger"
    java.util.List<CategorySpendDto> topCategories,
    java.util.List<MonthlyTrendDto> monthlyTrends,
    java.util.List<RecurringTransactionDto> recurringTransactions
) {}
