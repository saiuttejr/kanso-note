package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * DTO for top spending categories with amount and transaction count.
 */
public record CategorySpendDto(
    String category,
    BigDecimal amount,
    int transactionCount
) {}
