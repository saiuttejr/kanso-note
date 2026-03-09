package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Data transfer object for recurring transaction patterns with frequency and occurrence count.
 */
public record RecurringTransactionDto(
    String description,
    BigDecimal typicalAmount,
    int occurrenceCount,
    String frequency    // "Monthly", "Weekly", etc.
) {}
