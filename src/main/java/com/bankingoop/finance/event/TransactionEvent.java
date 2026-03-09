package com.bankingoop.finance.event;

import org.springframework.context.ApplicationEvent;

/**
 * Application event published on transaction lifecycle changes.
 * Enables decoupled audit logging, budget checking, and cache invalidation.
 */
public class TransactionEvent extends ApplicationEvent {

    public enum Action { CREATED, UPDATED, DELETED, IMPORTED, CLEARED }

    private final Action action;
    private final Long transactionId;
    private final String description;
    private final String category;
    private final java.math.BigDecimal amount;

    public TransactionEvent(Object source, Action action, Long transactionId,
                            String description, String category, java.math.BigDecimal amount) {
        super(source);
        this.action = action;
        this.transactionId = transactionId;
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    public Action getAction() { return action; }
    public Long getTransactionId() { return transactionId; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public java.math.BigDecimal getAmount() { return amount; }
}
