package com.bankingoop.finance.dto;

import java.math.BigDecimal;

/**
 * Data transfer object for flagged transactions with anomaly threshold and amount difference.
 */
public record UnusualTransactionDto(
    TransactionDto transaction,
    BigDecimal absoluteAmount,
    BigDecimal threshold
) {}
