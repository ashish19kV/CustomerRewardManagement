package com.retailer.reward.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Transactions done by customer")
public class TransactionDto {

	public TransactionDto() {

	}

	TransactionDto(String amount, String transactionDate) {
		this.amount = amount;
		this.transactionDate = transactionDate;
	}

	@NotNull(message = "Transaction amount is required")
	@NotBlank(message = "Transaction amount cannot be blank")
	@DecimalMin(value = "0.01", message = "Transaction amount must be greater than 0")
	@Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Amount must be a valid decimal number with up to 2 decimal places")
	@Schema(description = "Transaction amount", example = "120.00")
	private String amount;

	@NotBlank(message = "Transaction date is required")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Transaction date must be in yyyy-MM-dd format")
	@Schema(description = "Transaction date in yyyy-MM-dd format", example = "2024-01-15")
	private String transactionDate;

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

}
