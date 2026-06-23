package com.retailer.reward.dto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing reward points earned by a customer in a specific month.
 */
@Schema(description = "Reward points earned by a customer in a single calendar month")
public class MonthlyReward {

	@Schema(description = "Calendar year", example = "2024")
    private int year;

    @Schema(description = "Month number (1 = January … 12 = December)", example = "3")
    private int month;

    @Schema(description = "Full month name", example = "March")
    private String monthName;

    @Schema(description = "Total reward points earned in this month", example = "365")
    private long points;

	public MonthlyReward() {
	}

	public MonthlyReward(int year, int month, String monthName, long points) {
		this.year = year;
		this.month = month;
		this.monthName = monthName;
		this.points = points;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getMonthName() {
		return monthName;
	}

	public void setMonthName(String monthName) {
		this.monthName = monthName;
	}

	public long getPoints() {
		return points;
	}

	public void setPoints(long points) {
		this.points = points;
	}
}
