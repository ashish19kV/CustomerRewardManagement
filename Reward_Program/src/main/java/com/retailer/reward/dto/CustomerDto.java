package com.retailer.reward.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "customer details")
public class CustomerDto {
	
	@NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
	@Schema(description = "Full name of the customer", example = "Test One")
	private String customerName;

	@NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
	@Schema(description = "Email address of the customer", example = "test.one@example.com")
	private String customerEmail;
	
	public CustomerDto() {
	}
	
	public CustomerDto(String customerName, String customerEmail) {
		this.customerName = customerName;
		this.customerEmail = customerEmail;
	}
	
	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

}
