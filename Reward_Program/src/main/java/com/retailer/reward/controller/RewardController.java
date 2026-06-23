package com.retailer.reward.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailer.reward.dto.CustomerRewardSummary;
import com.retailer.reward.dto.CustomerTransaction;
import com.retailer.reward.dto.ErrorResponse;
import com.retailer.reward.service.RewardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing reward-program endpoints.
 *
 * <pre>
 * GET /api/rewards              → reward summary for ALL customers
 * GET /api/rewards/{customerId} → reward summary for ONE customer
 * GET /api/rewards/date-range/ → reward summary based on data range
 * POST /api/rewards/ → Persist Customer Transaction
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/rewards")
@Validated
@Tag(name = "Rewards Management", description = "APIs for calculating and retrieving customer reward points")
public class RewardController {

	@Autowired
    private RewardService rewardService;
		
	/**
	 * Returns reward points (monthly + total) for every customer.
	 */
	@Operation(summary = "Get rewards for all customers", description = "Returns the monthly and total reward points earned by every customer ")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved reward summaries for all customers", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CustomerRewardSummary.class)))),
			@ApiResponse(responseCode = "404", description = "Customers not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping
	public ResponseEntity<List<CustomerRewardSummary>> getAllRewards() {
		log.info("GET request for all customer rewards");
		List<CustomerRewardSummary> summaries = rewardService.getAllCustomerRewards();
		return ResponseEntity.ok(summaries);
	}

	/**
	 * Returns reward points (monthly + total) for a single customer.
	 *
	 * @param customerId the customer's unique identifier (positive Long)
	 */
	@Operation(summary = "Get rewards for a specific customer", description = "Returns the monthly and total reward points for the customer ")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved reward summary for the customer", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CustomerRewardSummary.class)))),
			@ApiResponse(responseCode = "404", description = "Customers not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid customer ID supplied ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/{customerId}")
	public ResponseEntity<CustomerRewardSummary> getRewardsByCustomer(
			@PathVariable @NotNull(message = "Customer ID is required") @Min(value = 1, message = "Customer ID must be a positive number") Long customerId) {
		log.info("GET request for customer rewards - ID: {}", customerId);
		CustomerRewardSummary summary = rewardService.getCustomerRewards(customerId);
		log.debug("CustomerRewardSummary found: {}", summary);
		return ResponseEntity.ok(summary);
	}

	// POST - Create a new Customer transaction to persist in database
	/**
	 * Returns reward points (monthly + total) for a single customer.
	 * @RequestBody customerTransaction to persist customer transaction
	 */
	@Operation(summary = "Create customer transaction ", description = "Sample data creation for rewards points")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfully create customer transaction", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CustomerTransaction.class)))),
			@ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid data supplied ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	@PostMapping
	public ResponseEntity<CustomerTransaction> createTransaction(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Customer transaction request body - all fields required", required = true) @Valid @RequestBody(required = true) CustomerTransaction customerTransaction) {

		log.info("POST request to create customer transaction for: {}",
				customerTransaction.getCustomerDto().getCustomerEmail());
		CustomerTransaction saved = rewardService.persistCustomerTransaction(customerTransaction);
		log.info("Customer transaction created successfully");
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}
	
// ==================== DATE RANGE ENDPOINTS ====================

	@GetMapping("/date-range")
	@Operation(summary = "Get Customers Rewards created within a date range", description = "Fetches all customer rewards created between the specified start and end dates")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved rewards for date range", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerRewardSummary.class))),
			@ApiResponse(responseCode = "400", description = "Invalid date range or customer ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	public ResponseEntity<List<CustomerRewardSummary>> getCustomerRewardsByDateRange(
			@RequestParam(required = true) @NotNull(message = "Start date is required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

			@RequestParam(required = true) @NotNull(message = "End date is required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.info("GET request for customer rewards between {} and {}", startDate, endDate);

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date must be before or equal to end date");
		}
		List<CustomerRewardSummary> response = rewardService.getCustomerRewardsByTransactionDateRange(startDate,
				endDate);
		return ResponseEntity.ok(response);
	}

}
