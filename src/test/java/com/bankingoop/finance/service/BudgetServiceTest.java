package com.bankingoop.finance.service;

import com.bankingoop.finance.dto.BudgetDto;
import com.bankingoop.finance.dto.BudgetStatusDto;
import com.bankingoop.finance.exception.DuplicateResourceException;
import com.bankingoop.finance.exception.ResourceNotFoundException;
import com.bankingoop.finance.repository.BudgetRepository;
import com.bankingoop.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BudgetServiceTest {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    void shouldCreateBudget() {
        BudgetDto budget = budgetService.createBudget("Dining", new BigDecimal("500.00"), new BigDecimal("80"));

        assertNotNull(budget);
        assertNotNull(budget.id());
        assertEquals("Dining", budget.category());
        assertEquals(new BigDecimal("500.00"), budget.monthlyLimit());
        assertEquals(new BigDecimal("80"), budget.alertThreshold());
        assertTrue(budget.enabled());
    }

    @Test
    void shouldRejectDuplicateBudgetCategory() {
        budgetService.createBudget("Dining", new BigDecimal("500.00"), new BigDecimal("80"));

        assertThrows(DuplicateResourceException.class,
                () -> budgetService.createBudget("Dining", new BigDecimal("300.00"), new BigDecimal("70")));
    }

    @Test
    void shouldUpdateBudget() {
        BudgetDto created = budgetService.createBudget("Transport", new BigDecimal("200.00"), new BigDecimal("80"));

        BudgetDto updated = budgetService.updateBudget(created.id(), new BigDecimal("350.00"), new BigDecimal("90"));

        assertEquals(new BigDecimal("350.00"), updated.monthlyLimit());
        assertEquals(new BigDecimal("90"), updated.alertThreshold());
    }

    @Test
    void shouldDeleteBudget() {
        BudgetDto budget = budgetService.createBudget("Streaming", new BigDecimal("50.00"), new BigDecimal("80"));

        budgetService.deleteBudget(budget.id());

        assertFalse(budgetRepository.existsById(budget.id()));
    }

    @Test
    void shouldThrowOnDeleteNonexistent() {
        assertThrows(ResourceNotFoundException.class,
                () -> budgetService.deleteBudget(9999L));
    }

    @Test
    void shouldReturnBudgetStatusesWithNoSpending() {
        budgetService.createBudget("Dining", new BigDecimal("500.00"), new BigDecimal("80"));

        List<BudgetStatusDto> statuses = budgetService.getBudgetStatuses();

        assertEquals(1, statuses.size());
        BudgetStatusDto status = statuses.get(0);
        assertEquals("Dining", status.category());
        assertEquals(0, status.spent().compareTo(BigDecimal.ZERO));
        assertEquals(0, status.utilizationPercent().compareTo(BigDecimal.ZERO));
        assertEquals("safe", status.alertLevel());
    }

    @Test
    void shouldListAllBudgets() {
        budgetService.createBudget("Dining", new BigDecimal("500.00"), new BigDecimal("80"));
        budgetService.createBudget("Transport", new BigDecimal("200.00"), new BigDecimal("80"));

        List<BudgetDto> budgets = budgetService.getAllBudgets();

        assertEquals(2, budgets.size());
    }
}
