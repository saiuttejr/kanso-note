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
 * Persistent categorization rule with priority and pattern type.
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
    // Initializes creation timestamp automatically on persist.
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- Constructors ---

    // Constructs a no-arg CategoryRuleEntity for JPA.
    public CategoryRuleEntity() {}

    // Constructs a CategoryRuleEntity with pattern, category, and priority.
    public CategoryRuleEntity(String patternType, String pattern, String category, int priority) {
        this.patternType = patternType;
        this.pattern = pattern;
        this.category = category;
        this.priority = priority;
    }

    // --- Getters and Setters ---

    // Returns the rule ID.
    public Long getId() { return id; }
    // Sets the rule ID.
    public void setId(Long id) { this.id = id; }

    // Returns the pattern type (KEYWORD, REGEX, etc).
    public String getPatternType() { return patternType; }
    // Sets the pattern type.
    public void setPatternType(String patternType) { this.patternType = patternType; }

    // Returns the matching pattern.
    public String getPattern() { return pattern; }
    // Sets the pattern.
    public void setPattern(String pattern) { this.pattern = pattern; }

    // Returns the category assigned by this rule.
    public String getCategory() { return category; }
    // Sets the category.
    public void setCategory(String category) { this.category = category; }

    // Returns the rule priority.
    public int getPriority() { return priority; }
    // Sets the priority.
    public void setPriority(int priority) { this.priority = priority; }

    // Checks if the rule is enabled.
    public boolean isEnabled() { return enabled; }
    // Enables or disables the rule.
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Checks if this is a default system rule.
    public boolean isDefault() { return isDefault; }
    // Marks the rule as default or custom.
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    // Returns the creation timestamp.
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Sets the creation timestamp.
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
