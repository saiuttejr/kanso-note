package com.bankingoop.finance.dto;

import java.math.BigDecimal;

import com.bankingoop.finance.entity.BudgetEntity;

/**
 * Budget DTO — carries budget data to the view/API layer.
 */
public record BudgetDto(
    Long id,
    String category,
    BigDecimal monthlyLimit,
    BigDecimal alertThreshold,
    boolean enabled
) {
    /** Converts budget entity to DTO for API and view layer use. */
    public static BudgetDto from(BudgetEntity e) {
        return new BudgetDto(
            e.getId(),
            e.getCategory(),
            e.getMonthlyLimit(),
            e.getAlertThreshold(),
            e.isEnabled()
        );
    }
}
