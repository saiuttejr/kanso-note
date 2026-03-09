package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Data transfer object for category spending with category name, amount, and transaction count.
 */
public record CategorySpendDto(
    String category,
    BigDecimal amount,
    int transactionCount
) {}
