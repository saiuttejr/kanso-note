package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * DTO for recurring transaction patterns based on description and amount similarity.
 * Groups transactions that appear at roughly the same amount on the same day each month.
 */
public record RecurringTransactionDto(
    String description,
    BigDecimal typicalAmount,
    int occurrenceCount,
    String frequency    // "Monthly", "Weekly", etc.
) {}
