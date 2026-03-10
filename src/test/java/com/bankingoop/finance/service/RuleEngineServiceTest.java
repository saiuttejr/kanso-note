package com.bankingoop.finance.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.bankingoop.finance.repository.CategoryRuleRepository;
import com.bankingoop.finance.repository.TransactionRepository;
import com.bankingoop.finance.entity.CategoryRuleEntity;

/**
 * Integration tests for the JPA-backed RuleEngineService.
 * Uses an in-memory H2 database via application-test.properties.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RuleEngineServiceTest {

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private CategoryRuleRepository categoryRuleRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        // Clear any leftover data between tests
        transactionRepository.deleteAll();
    }

    @Test
    void shouldApplyCustomRuleBeforeDefaultRule() {
        // Add a custom high-priority rule for "walmart" → "Household"
        ruleEngineService.addCustomRule("KEYWORD", "walmart", "Household", 20);
        categoryRuleRepository.flush();

        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "Walmart Supercenter", new BigDecimal("-20.00"), "");

        assertEquals("Household", result.category());
        assertNotNull(result.matchedRuleId());
    }

    @Test
    void shouldUseDefaultRuleWhenCustomDoesNotMatch() {
        // "uber" should match the default "Transport" rule seeded by Flyway
        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "Uber Trip", new BigDecimal("-12.00"), null);

        assertEquals("Transport", result.category());
    }

    @Test
    void shouldFallbackToIncomeCategoryForPositiveTransactions() {
        // No rule matches "Transfer from Employer", so positive amount → Income
        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "Transfer from Employer", new BigDecimal("1800.00"), "");

        assertEquals("Income", result.category());
        assertNull(result.matchedRuleId());
    }

    @Test
    void shouldUseExplicitCategoryWhenProvided() {
        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "Walmart Supercenter", new BigDecimal("-20.00"), "My Custom");

        assertEquals("My Custom", result.category());
        assertNull(result.matchedRuleId());
    }

    @Test
    void shouldResolveConflictByPriorityThenLongestMatch() {
        // Both rules match "uber eats delivery"; higher priority wins
        ruleEngineService.addCustomRule("KEYWORD", "uber", "Transport", 10);
        ruleEngineService.addCustomRule("KEYWORD", "uber eats", "Dining", 10);
        categoryRuleRepository.flush();

        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "Uber Eats Delivery", new BigDecimal("-25.00"), null);

        // Same priority → longest match wins → "uber eats" (9 chars) > "uber" (4 chars)
        assertEquals("Dining", result.category());
    }

    @Test
    void shouldSupportRegexPatternType() {
        ruleEngineService.addCustomRule("REGEX", "netflix|hulu|disney\\+", "Streaming", 15);
        categoryRuleRepository.flush();

        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "NETFLIX Monthly", new BigDecimal("-15.99"), null);

        assertEquals("Streaming", result.category());
    }

    @Test
    void higherPriorityOverridesLongerMatch() {
        // shorter keyword but higher priority should win
        ruleEngineService.addCustomRule("KEYWORD", "foo", "CategoryA", 20);
        ruleEngineService.addCustomRule("KEYWORD", "foobar", "CategoryB", 10);
        categoryRuleRepository.flush();

        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "FooBar Baz", new BigDecimal("-5.00"), null);

        // category name will be normalized to title case => "Categorya"
        assertEquals("Categorya", result.category());
    }

    @Test
    void disabledRuleIsIgnored() {
        CategoryRuleEntity custom = ruleEngineService.addCustomRule("KEYWORD", "ignoreme", "Misc", 50);
        ruleEngineService.toggleRule(custom.getId(), false);
        categoryRuleRepository.flush();

        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "This should ignoreme pattern", new BigDecimal("-1.00"), null);
        // fallback negative amount
        assertEquals("Uncategorized", result.category());
    }

    @Test
    void invalidRegexThrowsOnCreation() {
        assertThrows(IllegalArgumentException.class, () ->
                ruleEngineService.addCustomRule("REGEX", "*not a valid regex", "Broken", 5));
    }

    @Test
    void explicitCategoryNormalization() {
        RuleEngineService.MatchResult result = ruleEngineService.categorize(
                "Anything", new BigDecimal("0"), "  personal   LOANS  ");
        assertEquals("Personal Loans", result.category());
    }

    @Test
    void nullOrBlankDescriptionHandledGracefully() {
        // no exception, falls back based on amount
        RuleEngineService.MatchResult r1 = ruleEngineService.categorize(null, new BigDecimal("10"), null);
        assertEquals("Income", r1.category());

        RuleEngineService.MatchResult r2 = ruleEngineService.categorize("", new BigDecimal("-5"), null);
        assertEquals("Uncategorized", r2.category());
    }

    @Test
    void patternTypeCaseInsensitiveAndKeywordCaseInsensitive() {
        ruleEngineService.addCustomRule("regex", "TESTCASE", "CaseCat", 5);
        ruleEngineService.addCustomRule("KEYword", "MiXeDcAsE", "MixCat", 5);
        categoryRuleRepository.flush();

        RuleEngineService.MatchResult r1 = ruleEngineService.categorize("this testcase hits", BigDecimal.ZERO, null);
        // normalization lowercases trailing characters
        assertEquals("Casecat", r1.category());

        RuleEngineService.MatchResult r2 = ruleEngineService.categorize("mixedcase here", BigDecimal.ZERO, null);
        assertEquals("Mixcat", r2.category());
    }
}
