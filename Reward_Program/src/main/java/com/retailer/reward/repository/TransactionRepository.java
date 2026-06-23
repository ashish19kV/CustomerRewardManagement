package com.retailer.reward.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.retailer.reward.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	
	 @Query(
		        value = "SELECT t FROM Transaction t " +
		                "WHERE t.transactionDate BETWEEN :startDate AND :endDate",
		        countQuery = "SELECT COUNT(t) FROM Transaction t " +
		                     "WHERE t.transactionDate BETWEEN :startDate AND :endDate"
		    )
    List<Transaction> findByTransactionDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
	   
}
