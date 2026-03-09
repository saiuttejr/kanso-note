package com.bankingoop.finance.service;

import com.bankingoop.finance.dto.MonthlyTrendDto;
import com.bankingoop.finance.dto.TransactionDto;
import com.bankingoop.finance.repository.BudgetRepository;
import com.bankingoop.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinanceTrackerServiceTest {

    @Autowired
    private FinanceTrackerService financeTrackerService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        budgetRepository.deleteAll();
    }

    @Test
    void shouldAddManualTransaction() {
        TransactionDto dto = financeTrackerService.addManualTransaction(
                LocalDate.now(), "Coffee Shop", new BigDecimal("-5.50"), "Dining");

        assertNotNull(dto);
        assertNotNull(dto.id());
        assertEquals("Coffee Shop", dto.description());
        assertEquals("Dining", dto.category());
        assertEquals(new BigDecimal("-5.50"), dto.amount());
    }

    @Test
    void shouldAutoCategorizeIncomeTransaction() {
        TransactionDto dto = financeTrackerService.addManualTransaction(
                LocalDate.now(), "Paycheck Deposit", new BigDecimal("3000.00"), null);

        assertEquals("Income", dto.category());
    }

    @Test
    void shouldTrackIncomeAndExpense() {
        financeTrackerService.addManualTransaction(
                LocalDate.now(), "Salary", new BigDecimal("3000.00"), "Income");
        financeTrackerService.addManualTransaction(
                LocalDate.now(), "Grocery Store", new BigDecimal("-150.00"), "Groceries");

        assertEquals(0, financeTrackerService.getTotalIncome().compareTo(new BigDecimal("3000.00")));
        assertEquals(0, financeTrackerService.getTotalExpense().compareTo(new BigDecimal("150.00")));
        assertTrue(financeTrackerService.getNetFlow().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldUpdateTransaction() {
        TransactionDto created = financeTrackerService.addManualTransaction(
                LocalDate.now(), "Old Description", new BigDecimal("-10.00"), "Other");

        TransactionDto updated = financeTrackerService.updateTransaction(
                created.id(), null, "New Description", new BigDecimal("-20.00"), "Dining");

        assertEquals("New Description", updated.description());
        assertEquals(new BigDecimal("-20.00"), updated.amount());
    }

    @Test
    void shouldDeleteTransaction() {
        TransactionDto dto = financeTrackerService.addManualTransaction(
                LocalDate.now(), "Temp", new BigDecimal("-1.00"), "Other");

        financeTrackerService.deleteTransaction(dto.id());

        assertEquals(0, financeTrackerService.getTransactionCount());
    }

    @Test
    void shouldClearAllTransactions() {
        financeTrackerService.addManualTransaction(LocalDate.now(), "A", new BigDecimal("-1.00"), "Other");
        financeTrackerService.addManualTransaction(LocalDate.now(), "B", new BigDecimal("-2.00"), "Other");

        int cleared = financeTrackerService.clearTransactions();

        assertEquals(2, cleared);
        assertEquals(0, financeTrackerService.getTransactionCount());
    }

    @Test
    void shouldComputeMonthlyTrends() {
        financeTrackerService.addManualTransaction(
                LocalDate.now(), "Salary", new BigDecimal("3000.00"), "Income");
        financeTrackerService.addManualTransaction(
                LocalDate.now(), "Rent", new BigDecimal("-1200.00"), "Housing");

        List<MonthlyTrendDto> trends = financeTrackerService.getMonthlyTrends();

        assertFalse(trends.isEmpty());
        MonthlyTrendDto current = trends.get(trends.size() - 1);
        assertTrue(current.income().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(current.expense().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldRejectTransactionWithBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> financeTrackerService.addManualTransaction(
                        LocalDate.now(), "   ", new BigDecimal("-5.00"), null));
    }

    @Test
    void shouldRejectTransactionWithNullDate() {
        assertThrows(IllegalArgumentException.class,
                () -> financeTrackerService.addManualTransaction(
                        null, "Test", new BigDecimal("-5.00"), null));
    }

    @Test
    void shouldGetTransactionsFilteredByDateRange() {
        financeTrackerService.addManualTransaction(
                LocalDate.of(2025, 1, 15), "Jan expense", new BigDecimal("-50.00"), "Other");
        financeTrackerService.addManualTransaction(
                LocalDate.of(2025, 3, 15), "Mar expense", new BigDecimal("-30.00"), "Other");

        List<TransactionDto> filtered = financeTrackerService.getTransactions(
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31));

        assertEquals(1, filtered.size());
        assertEquals("Mar expense", filtered.get(0).description());
    }

    @Test
    void shouldUndoLastManualTransaction() {
        financeTrackerService.addManualTransaction(
                LocalDate.now(), "Undo me", new BigDecimal("-10.00"), "Other");

        assertTrue(financeTrackerService.canUndo());
        String result = financeTrackerService.undoLastAction();

        assertNotNull(result);
        assertEquals(0, financeTrackerService.getTransactionCount());
    }

    @Test
    void shouldComputeSavingsRate() {
        financeTrackerService.addManualTransaction(
                LocalDate.now(), "Salary", new BigDecimal("5000.00"), "Income");
        financeTrackerService.addManualTransaction(
                LocalDate.now(), "Rent", new BigDecimal("-2000.00"), "Housing");

        BigDecimal rate = financeTrackerService.getSavingsRatePercent();

        assertNotNull(rate);
        // (5000 - 2000) / 5000 * 100 = 60%
        assertEquals(0, rate.compareTo(new BigDecimal("60.00")));
    }
}
