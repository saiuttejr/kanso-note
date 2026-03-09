package com.bankingoop.finance.dto;

/**
 * DTO for category rules displayed in UI and REST endpoints.
 * Exposes only what the view needs: pattern info, category, priority, and type.
 */
public record CategoryRuleDto(
    Long id,
    String patternType,
    String pattern,
    String category,
    int priority,
    boolean enabled,
    boolean isDefault
) {
    public static CategoryRuleDto from(com.bankingoop.finance.entity.CategoryRuleEntity e) {
        return new CategoryRuleDto(
            e.getId(),
            e.getPatternType(),
            e.getPattern(),
            e.getCategory(),
            e.getPriority(),
            e.isEnabled(),
            e.isDefault()
        );
    }
}
