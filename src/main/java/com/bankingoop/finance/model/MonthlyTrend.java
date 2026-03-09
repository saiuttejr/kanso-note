package com.bankingoop.finance.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

// Aggregated monthly income and expense data with net flow calculation for trend analysis.
public class MonthlyTrend {
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");

    private final YearMonth month;
    private final BigDecimal income;
    private final BigDecimal expense;

    // Constructs a MonthlyTrend with month, income, and expense amounts.
    public MonthlyTrend(YearMonth month, BigDecimal income, BigDecimal expense) {
        this.month = month;
        this.income = income;
        this.expense = expense;
    }

    // Returns the month and year of this trend data.
    public YearMonth getMonth() {
        return month;
    }

    // Returns the total income for this month.
    public BigDecimal getIncome() {
        return income;
    }

    // Returns the total expense for this month.
    public BigDecimal getExpense() {
        return expense;
    }

    // Calculates and returns the net flow (income minus expense).
    public BigDecimal getNet() {
        return income.subtract(expense);
    }

    // Checks if the net flow is positive or zero (savings month).
    public boolean isPositiveNet() {
        return getNet().signum() >= 0;
    }

    // Returns the formatted month label (e.g., "Jan 2024").
    public String getLabel() {
        return month.format(LABEL_FORMAT);
    }
}
