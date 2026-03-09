package com.bankingoop.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// Immutable data structure representing a single row from CSV file import.
public class CsvTransactionRow {
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;
    private final String category;

    // Constructs a CsvTransactionRow from parsed CSV fields.
    public CsvTransactionRow(LocalDate date, String description, BigDecimal amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    // Returns the transaction date from CSV.
    public LocalDate getDate() {
        return date;
    }

    // Returns the transaction description from CSV.
    public String getDescription() {
        return description;
    }

    // Returns the transaction amount from CSV.
    public BigDecimal getAmount() {
        return amount;
    }

    // Returns the category from CSV.
    public String getCategory() {
        return category;
    }
}
