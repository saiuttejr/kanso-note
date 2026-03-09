package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Budget status DTO — combines a budget with its real-time spending data.
 *
 * Design decision — precomputed status:
 *   We compute the spent amount, utilization percentage, and alert level
 *   in the service layer and pass the result to the view. This keeps the
 *   template logic simple and the status easy to test.
 */
public record BudgetStatusDto(
    Long budgetId,
    String category,
    BigDecimal monthlyLimit,
    BigDecimal spent,
    BigDecimal remaining,
    BigDecimal utilizationPercent,
    String alertLevel     // "safe", "warning", "exceeded"
) {
    public boolean isExceeded() {
        return "exceeded".equals(alertLevel);
    }

    public boolean isWarning() {
        return "warning".equals(alertLevel);
    }
}
