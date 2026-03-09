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

    /** Creates application event for transaction lifecycle changes (create, update, delete, import). */
    public TransactionEvent(Object source, Action action, Long transactionId,
                            String description, String category, java.math.BigDecimal amount) {
        super(source);
        this.action = action;
        this.transactionId = transactionId;
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    /** Returns the action type (CREATED, UPDATED, DELETED, IMPORTED, CLEARED). */
    public Action getAction() { return action; }
    
    /** Returns the ID of the affected transaction. */
    public Long getTransactionId() { return transactionId; }
    
    /** Returns the transaction description. */
    public String getDescription() { return description; }
    
    /** Returns the transaction category. */
    public String getCategory() { return category; }
    
    /** Returns the transaction amount (positive for income, negative for expense). */
    public java.math.BigDecimal getAmount() { return amount; }
}
