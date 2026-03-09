package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * DTO displaying a flagged transaction with anomaly threshold and amount.
 */
public record UnusualTransactionDto(
    TransactionDto transaction,
    BigDecimal absoluteAmount,
    BigDecimal threshold
) {}
