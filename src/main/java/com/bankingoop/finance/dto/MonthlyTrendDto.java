package com.bankingoop.finance.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Supports: monthly trend display on the dashboard with MoM delta + rolling average.
 */
public record MonthlyTrendDto(
    YearMonth month,
    String label,
    BigDecimal income,
    BigDecimal expense,
    BigDecimal net,
    boolean positiveNet,
    BigDecimal momDeltaPercent,     // month-over-month % change in total spend
    BigDecimal rollingAvgExpense    // 3-month rolling average of expenses
) {
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");

    public static MonthlyTrendDto of(YearMonth month, BigDecimal income, BigDecimal expense,
                                     BigDecimal momDeltaPercent, BigDecimal rollingAvgExpense) {
        BigDecimal net = income.subtract(expense);
        return new MonthlyTrendDto(
            month,
            month.format(LABEL_FORMAT),
            income,
            expense,
            net,
            net.signum() >= 0,
            momDeltaPercent,
            rollingAvgExpense
        );
    }
}
