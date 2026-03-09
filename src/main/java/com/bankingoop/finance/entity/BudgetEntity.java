package com.bankingoop.finance.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Budget entity with per-category monthly spending limit and alert threshold.
 */
@Entity
@Table(name = "budget", indexes = {
    @Index(name = "idx_budget_category", columnList = "category")
})
public class BudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String category;

    @Column(name = "monthly_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "alert_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal alertThreshold = new BigDecimal("80.00");

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    // Initializes creation and update timestamps on persist.
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    // Updates the timestamp when the budget is modified.
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters / Setters ---

    // Returns the budget ID.
    public Long getId() { return id; }
    // Sets the budget ID.
    public void setId(Long id) { this.id = id; }

    // Returns the category for this budget.
    public String getCategory() { return category; }
    // Sets the category.
    public void setCategory(String category) { this.category = category; }

    // Returns the monthly spending limit.
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    // Sets the monthly limit.
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    // Returns the alert threshold percentage.
    public BigDecimal getAlertThreshold() { return alertThreshold; }
    // Sets the alert threshold.
    public void setAlertThreshold(BigDecimal alertThreshold) { this.alertThreshold = alertThreshold; }

    // Checks if the budget is enabled.
    public boolean isEnabled() { return enabled; }
    // Enables or disables the budget.
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Returns the creation timestamp.
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Returns the last update timestamp.
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
