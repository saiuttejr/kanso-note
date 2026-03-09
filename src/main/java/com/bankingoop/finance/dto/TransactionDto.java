package com.bankingoop.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Supports: clean separation — controllers never expose JPA entities to the view.
 * DTO carries only the fields the dashboard template needs.
 */
public record TransactionDto(
    Long id,
    LocalDate date,
    String description,
    BigDecimal amount,
    String category,
    Long matchedRuleId,
    boolean income,
    boolean expense
) {
    public static TransactionDto from(com.bankingoop.finance.entity.TransactionEntity e) {
        return new TransactionDto(
            e.getId(),
            e.getDate(),
            e.getDescription(),
            e.getAmount(),
            e.getCategory(),
            e.getMatchedRuleId(),
            e.isIncome(),
            e.isExpense()
        );
    }
}
