package com.bankingoop.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CsvTransactionRow {
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;
    private final String category;

    public CsvTransactionRow(LocalDate date, String description, BigDecimal amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
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
}
