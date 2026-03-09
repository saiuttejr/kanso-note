package com.bankingoop.finance.event;

import org.springframework.context.ApplicationEvent;

/**
 * Application event published when a budget is created, updated, or when spending exceeds a threshold.
 */
public class BudgetEvent extends ApplicationEvent {

    public enum Action { CREATED, UPDATED, DELETED, THRESHOLD_REACHED, EXCEEDED }

    private final Action action;
    private final Long budgetId;
    private final String category;
    private final String details;

    /** Creates application event for budget changes (create, update, delete, threshold alerts). */
    public BudgetEvent(Object source, Action action, Long budgetId, String category, String details) {
        super(source);
        this.action = action;
        this.budgetId = budgetId;
        this.category = category;
        this.details = details;
    }

    /** Returns the action type (CREATED, UPDATED, DELETED, THRESHOLD_REACHED, EXCEEDED). */
    public Action getAction() { return action; }
    
    /** Returns the ID of the affected budget. */
    public Long getBudgetId() { return budgetId; }
    
    /** Returns the budget category name. */
    public String getCategory() { return category; }
    
    /** Returns event details describing the change or threshold violation. */
    public String getDetails() { return details; }
}
