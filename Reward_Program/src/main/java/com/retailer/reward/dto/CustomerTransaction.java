package com.retailer.reward.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


@Schema(description = "Saving customer transactions")
public class CustomerTransaction {

	public CustomerTransaction() {

	}

	public CustomerTransaction(CustomerDto customerDto, List<TransactionDto> transactionsDto) {
		this.customerDto = customerDto;
		this.transactionsDto = transactionsDto;
	}

	@Valid
	@NotNull(message = "Customer details are required")
	@Schema(description = "Customer information")
	private CustomerDto customerDto;

	@Valid
	@NotNull(message = "Transactions list is required")
	@NotEmpty(message = "At least one transaction is required")
	@Schema(description = "List of transactions")
	private List<TransactionDto> transactionsDto;

	public CustomerDto getCustomerDto() {
		return this.customerDto;
	}

	public void setCustomerDto(CustomerDto customerDto) {
		this.customerDto = customerDto;
	}

	public List<TransactionDto> getTransactionsDto() {
		return this.transactionsDto;
	}

	public void setTransactionsDto(List<TransactionDto> transactionsDto) {
		this.transactionsDto = transactionsDto;
	}

}
