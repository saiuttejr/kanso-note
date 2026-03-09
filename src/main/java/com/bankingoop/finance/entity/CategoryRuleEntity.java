package com.bankingoop.finance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Supports: deterministic rule engine with priority + regex (interview talking point).
 *
 * Design decision — persistent rules with priority and pattern type:
 *   Rules are stored in H2 so custom rules survive restarts. The priority integer
 *   and pattern_type (KEYWORD vs REGEX) enable deterministic conflict resolution:
 *   highest priority wins; on tie, longest matching pattern wins.
 *   The is_default flag separates seed data from user-created rules.
 */
@Entity
@Table(name = "category_rule")
public class CategoryRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern_type", nullable = false, length = 10)
    private String patternType = "KEYWORD";

    @Column(nullable = false, length = 255)
    private String pattern;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private int priority = 0;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- Constructors ---

    public CategoryRuleEntity() {}

    public CategoryRuleEntity(String patternType, String pattern, String category, int priority) {
        this.patternType = patternType;
        this.pattern = pattern;
        this.category = category;
        this.priority = priority;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatternType() { return patternType; }
    public void setPatternType(String patternType) { this.patternType = patternType; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
