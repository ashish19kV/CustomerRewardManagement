package com.retailer.reward.exception;

/**
 * Thrown when a customer ID supplied in the request is invalid
 * (e.g. null, zero, or negative).
 */
public class InvalidCustomerIdException extends RuntimeException {

   	private static final long serialVersionUID = 1L;
	private final String providedValue;

    public InvalidCustomerIdException(String providedValue) {
        super("Invalid customer ID provided: '" + providedValue + "'. ID must be a positive number.");
        this.providedValue = providedValue;
    }

    public String getProvidedValue() {
        return providedValue;
    }
}
