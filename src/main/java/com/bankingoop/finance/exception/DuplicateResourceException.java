package com.bankingoop.finance.exception;

/**
 * Thrown when a budget already exists for a given category.
 */
public class DuplicateResourceException extends RuntimeException {
    private final String resourceType;
    private final String conflictField;

    /** Thrown when creating a resource that already exists for given field (e.g., duplicate budget). */
    public DuplicateResourceException(String resourceType, String conflictField) {
        super(resourceType + " already exists for: " + conflictField);
        this.resourceType = resourceType;
        this.conflictField = conflictField;
    }

    /** Returns the type of resource that caused the conflict. */
    public String getResourceType() { return resourceType; }
    
    /** Returns the field value causing the duplicate conflict. */
}
