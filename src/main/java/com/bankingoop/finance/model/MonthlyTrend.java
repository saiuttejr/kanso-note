package com.bankingoop.finance.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class MonthlyTrend {
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");

    private final YearMonth month;
    private final BigDecimal income;
    private final BigDecimal expense;

    public MonthlyTrend(YearMonth month, BigDecimal income, BigDecimal expense) {
        this.month = month;
        this.income = income;
        this.expense = expense;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public BigDecimal getNet() {
        return income.subtract(expense);
    }

    public boolean isPositiveNet() {
        return getNet().signum() >= 0;
    }

    public String getLabel() {
        return month.format(LABEL_FORMAT);
    }
}
