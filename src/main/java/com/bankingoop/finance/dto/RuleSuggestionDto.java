package com.bankingoop.finance.dto;

/**
 * Supports: auto-suggest rules (D18) — groups uncategorized descriptions and suggests a rule.
 */
public record RuleSuggestionDto(
    String pattern,
    int matchCount,
    String suggestedCategory
) {}
