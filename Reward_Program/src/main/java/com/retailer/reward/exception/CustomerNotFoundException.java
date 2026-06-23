package com.retailer.reward.exception;

/**
 * Thrown when a requested customer ID does not exist in the system.
 */
public class CustomerNotFoundException extends RuntimeException {

    
	private static final long serialVersionUID = 1L;
	private final Long customerId;

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with ID: " + customerId);
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
