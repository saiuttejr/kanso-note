package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Supports: top-N spending categories widget on the dashboard.
 */
public record CategorySpendDto(
    String category,
    BigDecimal amount,
    int transactionCount
) {}
