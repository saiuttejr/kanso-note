package com.bankingoop.finance.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Transaction entity persisted to local H2 database with category mapping.
 */
@Entity
@Table(name = "transaction", indexes = {
    @Index(name = "idx_transaction_date", columnList = "date"),
    @Index(name = "idx_transaction_category", columnList = "category")
})
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 100)
    private String category = "Uncategorized";

    @Column(name = "matched_rule_id")
    private Long matchedRuleId;

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

    // Constructs a no-arg TransactionEntity for JPA.
    public TransactionEntity() {}

    // Constructs a TransactionEntity with core transaction data.
    public TransactionEntity(LocalDate date, String description, BigDecimal amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    // --- Business helpers ---

    // Checks if the transaction amount is negative (expense).
    public boolean isExpense() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // Checks if the transaction amount is positive (income).
    public boolean isIncome() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    // --- Getters and Setters ---

    // Returns the database-assigned ID.
    public Long getId() { return id; }
    // Sets the database ID.
    public void setId(Long id) { this.id = id; }

    // Returns the transaction date.
    public LocalDate getDate() { return date; }
    // Sets the transaction date.
    public void setDate(LocalDate date) { this.date = date; }

    // Returns the transaction description.
    public String getDescription() { return description; }
    // Sets the transaction description.
    public void setDescription(String description) { this.description = description; }

    // Returns the transaction amount.
    public BigDecimal getAmount() { return amount; }
    // Sets the transaction amount.
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    // Returns the assigned category.
    public String getCategory() { return category; }
    // Sets the category.
    public void setCategory(String category) { this.category = category; }

    // Returns the matched rule ID if auto-categorized.
    public Long getMatchedRuleId() { return matchedRuleId; }
    // Sets the matched rule ID.
    public void setMatchedRuleId(Long matchedRuleId) { this.matchedRuleId = matchedRuleId; }

    // Returns the creation timestamp.
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Sets the creation timestamp.
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
