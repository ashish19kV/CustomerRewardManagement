package com.retailer.reward.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Represents a single purchase transaction made by a customer.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
	
	@ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
	
	@Column(nullable = false)
    private BigDecimal  amount;
	
	@Column(name = "transaction_date")
    private LocalDate transactionDate;
	
	@PrePersist
    public void prePersist() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
    }
    
 }
