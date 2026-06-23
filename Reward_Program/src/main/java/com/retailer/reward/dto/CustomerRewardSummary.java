package com.retailer.reward.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO representing the complete reward summary for a customer, including
 * monthly breakdown and total points.
 */
@Schema(description = "Complete reward summary for a customer including per-month breakdown and grand total")
public class CustomerRewardSummary {

	@Schema(description = "Unique identifier of the customer", example = "1")
	private Long customerId;

	@Schema(description = "Full name of the customer", example = "Test One")
	private String customerName;

	@Schema(description = "Email address of the customer", example = "test.one@example.com")
	private String customerEmail;

	@Schema(description = "List of reward points earned per month")
	private List<MonthlyReward> monthlyRewards;

	@Schema(description = "Grand total of reward points earned across all months", example = "670")
	private long totalPoints;

	public CustomerRewardSummary() {
	}

	public CustomerRewardSummary(Long customerId, String customerName, String customerEmail,
			List<MonthlyReward> monthlyRewards, long totalPoints) {
		this.customerId = customerId;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.monthlyRewards = monthlyRewards;
		this.totalPoints = totalPoints;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
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

	public List<MonthlyReward> getMonthlyRewards() {
		return monthlyRewards;
	}

	public void setMonthlyRewards(List<MonthlyReward> monthlyRewards) {
		this.monthlyRewards = monthlyRewards;
	}

	public long getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(long totalPoints) {
		this.totalPoints = totalPoints;
	}
}
