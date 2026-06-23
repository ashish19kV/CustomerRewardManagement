package com.retailer.reward.model;

/**
 * Represents a customer in the rewards program.
 */

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false, unique = true)
    private String email;

 }
