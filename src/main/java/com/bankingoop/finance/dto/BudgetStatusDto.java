package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Budget status DTO combining budget with real-time spending data and alert status.\n */
public record BudgetStatusDto(
    Long budgetId,
    String category,
    BigDecimal monthlyLimit,
    BigDecimal spent,
    BigDecimal remaining,
    BigDecimal utilizationPercent,
    String alertLevel     // "safe", "warning", "exceeded"
) {
    /** Returns true if budget alert level is 'exceeded'. */
    public boolean isExceeded() {
        return "exceeded".equals(alertLevel);
    }

    /** Returns true if budget alert level is 'warning'. */
    public boolean isWarning() {
        return "warning".equals(alertLevel);
    }
}
