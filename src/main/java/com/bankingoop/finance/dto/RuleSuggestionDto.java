package com.bankingoop.finance.dto;

/**
 * DTO for auto-suggested rules based on uncategorized transaction clusters.
 */
public record RuleSuggestionDto(
    String pattern,
    int matchCount,
    String suggestedCategory
) {}
