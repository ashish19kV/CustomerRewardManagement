package com.retailer.reward.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.retailer.reward.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler that intercepts exceptions thrown anywhere in the
 * application
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomerNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex,
			HttpServletRequest request) {

		log.error("Customer Not found : {}", ex.getMessage());

		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// 400 – Invalid customer ID (non-numeric / negative)
	// ─────────────────────────────────────────────────────────────────────────
	@ExceptionHandler(InvalidCustomerIdException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleInvalidCustomerId(InvalidCustomerIdException ex,
			HttpServletRequest request) {

		log.error("Invalid Customer Id : {}", ex.getMessage());
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// 400 – Path variable type mismatch (e.g. /api/rewards/abc)
	// ─────────────────────────────────────────────────────────────────────────
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpServletRequest request) {

		log.error("Method Arguments type not match : {}", ex.getMessage());
		String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.", ex.getValue(),
				ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", message,
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// 500 – Catch-all for unexpected exceptions
	// ─────────────────────────────────────────────────────────────────────────
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

		log.error("Generic Exception error : {}", ex.getMessage());
		ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR",
				"An unexpected error occurred. Please try again later.", request.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
	
   // This fires BEFORE @Valid when body is missing or unparseable
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleEmptyRequestBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.error("Request body is missing or unreadable: {}", ex.getMessage());
             
    	ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request",
    			"Request body is required and must not be blank", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.error("Validation failed: {}", ex.getMessage());
               
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed",
    			"Request body validation failed. Please check the field errors.", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

   
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.error("Constraint violation: {}", ex.getMessage());
     
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed",
    			"Parameter validation failed", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  

}
