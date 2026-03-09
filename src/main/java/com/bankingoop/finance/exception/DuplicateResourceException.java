package com.bankingoop.finance.exception;

/**
 * Thrown when a budget already exists for a given category.
 */
public class DuplicateResourceException extends RuntimeException {
    private final String resourceType;
    private final String conflictField;

    public DuplicateResourceException(String resourceType, String conflictField) {
        super(resourceType + " already exists for: " + conflictField);
        this.resourceType = resourceType;
        this.conflictField = conflictField;
    }

    public String getResourceType() { return resourceType; }
    public String getConflictField() { return conflictField; }
}
