package com.bankingoop.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// Immutable value object representing a single financial transaction with date, amount, description, and category.
public class Transaction {
    private final long id;
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;
    private final String category;

    // Constructs a Transaction with all required fields.
    public Transaction(long id, LocalDate date, String description, BigDecimal amount, String category) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    // Returns the unique identifier for this transaction.
    public long getId() {
        return id;
    }

    // Returns the transaction date.
    public LocalDate getDate() {
        return date;
    }

    // Returns the transaction description or memo.
    public String getDescription() {
        return description;
    }

    // Returns the transaction amount (positive for income, negative for expense).
    public BigDecimal getAmount() {
        return amount;
    }

    // Returns the category assignment for this transaction.
    public String getCategory() {
        return category;
    }

    // Checks if this transaction represents a negative amount (expense or debit).
    public boolean isExpense() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // Checks if this transaction represents a positive amount (income or credit).
    public boolean isIncome() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
