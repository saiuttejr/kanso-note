package com.bankingoop.finance.event;

import org.springframework.context.ApplicationEvent;

/**
 * Application event published when a transaction is created, updated, or deleted.
 *
 * Design decision — Spring Application Events:
 *   Using Spring's built-in event system decouples the core transaction logic
 *   from cross-cutting concerns like audit logging, budget checking, and cache
 *   invalidation. Each listener can be added/removed independently without
 *   modifying the service that publishes the event.
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
