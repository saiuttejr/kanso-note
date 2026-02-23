package com.bankingoop.finance.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleEngineServiceTest {

    private final RuleEngineService ruleEngineService = new RuleEngineService();

    @Test
    void shouldApplyCustomRuleBeforeDefaultRule() {
        ruleEngineService.addCustomRule("walmart", "Household");

        String category = ruleEngineService.categorize("Walmart Supercenter", new BigDecimal("-20.00"), "");

        assertEquals("Household", category);
    }

    @Test
    void shouldUseDefaultRuleWhenCustomDoesNotMatch() {
        String category = ruleEngineService.categorize("Uber Trip", new BigDecimal("-12.00"), null);

        assertEquals("Transport", category);
    }

    @Test
    void shouldFallbackToIncomeCategoryForPositiveTransactions() {
        String category = ruleEngineService.categorize("Transfer from Employer", new BigDecimal("1800.00"), "");

        assertEquals("Income", category);
    }
}
