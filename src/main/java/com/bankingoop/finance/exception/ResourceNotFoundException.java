package com.bankingoop.finance.exception;

/**
 * Thrown when a requested resource (transaction, budget, rule) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final Object resourceId;

    /** Thrown when a requested resource (transaction, budget, rule) is not found by ID. */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found with id: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /** Returns the type of resource that was not found. */
    public String getResourceType() { return resourceType; }
    
    /** Returns the ID that was used in the unsuccessful lookup. */
}
