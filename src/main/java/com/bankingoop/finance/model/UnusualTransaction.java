package com.bankingoop.finance.model;

import java.math.BigDecimal;

public class UnusualTransaction {
    private final Transaction transaction;
    private final BigDecimal absoluteAmount;
    private final BigDecimal threshold;

    public UnusualTransaction(Transaction transaction, BigDecimal absoluteAmount, BigDecimal threshold) {
        this.transaction = transaction;
        this.absoluteAmount = absoluteAmount;
        this.threshold = threshold;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public BigDecimal getAbsoluteAmount() {
        return absoluteAmount;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }
}
