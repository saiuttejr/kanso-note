package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Data transfer object aggregating total income, expenses, trends, and top categories.
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
