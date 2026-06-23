package com.retailer.reward.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Error response body returned by the global exception handler.
 */
@Schema(description = "Standard error response returned when an API call fails")
public class ErrorResponse {

	@Schema(description = "HTTP status code", example = "404")
	private int status;

	@Schema(description = "Short error category", example = "NOT_FOUND")
	private String error;

	@Schema(description = "Human-readable error message", example = "Customer not found with ID: 99")
	private String message;

	@Schema(description = "Request path that triggered the error", example = "/api/rewards/99")
	private String path;

	@Schema(description = "Timestamp when the error occurred", example = "2026-03-15T10:30:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime timestamp;

	public ErrorResponse() {
		this.timestamp = LocalDateTime.now();
	}

	public ErrorResponse(int status, String error, String message, String path) {
		this();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
}
