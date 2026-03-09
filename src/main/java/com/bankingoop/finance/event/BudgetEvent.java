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

    public BudgetEvent(Object source, Action action, Long budgetId, String category, String details) {
        super(source);
        this.action = action;
        this.budgetId = budgetId;
        this.category = category;
        this.details = details;
    }

    public Action getAction() { return action; }
    public Long getBudgetId() { return budgetId; }
    public String getCategory() { return category; }
    public String getDetails() { return details; }
}
