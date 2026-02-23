package com.bankingoop.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private final long id;
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;
    private final String category;

    public Transaction(long id, LocalDate date, String description, BigDecimal amount, String category) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public boolean isExpense() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isIncome() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
