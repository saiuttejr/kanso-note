package com.bankingoop.finance.dto;

/**
 * Data transfer object for auto-suggested categorization rules with match count.
 */
public record RuleSuggestionDto(
    String pattern,
    int matchCount,
    String suggestedCategory
) {}
