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
 * Supports: offline persistence — transactions survive app restarts (interview talking point).
 *
 * Design decision — JPA entity backed by H2 file-mode:
 *   We store every transaction in a local H2 database file so data persists across
 *   restarts without any cloud dependency. The matched_rule_id column links to the
 *   rule that categorised this transaction, enabling explainability ("why was this
 *   categorised as Groceries?").
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
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- Constructors ---

    public TransactionEntity() {}

    public TransactionEntity(LocalDate date, String description, BigDecimal amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    // --- Business helpers ---

    public boolean isExpense() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isIncome() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getMatchedRuleId() { return matchedRuleId; }
    public void setMatchedRuleId(Long matchedRuleId) { this.matchedRuleId = matchedRuleId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
