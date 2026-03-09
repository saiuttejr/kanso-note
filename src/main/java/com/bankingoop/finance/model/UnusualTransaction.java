package com.bankingoop.finance.model;

import java.math.BigDecimal;

// Represents a flagged transaction that exceeds the anomaly detection threshold.
public class UnusualTransaction {
    private final Transaction transaction;
    private final BigDecimal absoluteAmount;
    private final BigDecimal threshold;

    // Constructs an UnusualTransaction with base transaction and threshold info.
    public UnusualTransaction(Transaction transaction, BigDecimal absoluteAmount, BigDecimal threshold) {
        this.transaction = transaction;
        this.absoluteAmount = absoluteAmount;
        this.threshold = threshold;
    }

    // Returns the underlying transaction object.
    public Transaction getTransaction() {
        return transaction;
    }

    // Returns the absolute transaction amount.
    public BigDecimal getAbsoluteAmount() {
        return absoluteAmount;
    }

    // Returns the threshold that was exceeded.
    public BigDecimal getThreshold() {
        return threshold;
    }
}
