package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Supports: anomaly detection display — shows why a transaction was flagged as unusual.
 */
public record UnusualTransactionDto(
    TransactionDto transaction,
    BigDecimal absoluteAmount,
    BigDecimal threshold
) {}
