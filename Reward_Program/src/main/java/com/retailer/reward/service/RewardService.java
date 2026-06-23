package com.retailer.reward.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailer.reward.dto.CustomerDto;
import com.retailer.reward.dto.CustomerRewardSummary;
import com.retailer.reward.dto.CustomerTransaction;
import com.retailer.reward.dto.MonthlyReward;
import com.retailer.reward.dto.TransactionDto;
import com.retailer.reward.exception.CustomerNotFoundException;
import com.retailer.reward.exception.InvalidCustomerIdException;
import com.retailer.reward.model.Customer;
import com.retailer.reward.model.Transaction;
import com.retailer.reward.repository.CustomerRepository;
import com.retailer.reward.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for calculating reward points.
 *
 * Reward rules:
 *   $0 – $50   : 0 points
 *   $50 – $100 : 1 point per dollar spent over $50
 *   Over $100  : 1 point per dollar between $50-$100 (max 50 pts)
 *                + 2 points per dollar over $100</li>
 *   Example: $120 purchase → 2×$20 + 1×$50 = 90 points
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RewardService {

	@Autowired
    private CustomerRepository customerRepository;
	
	@Autowired
    private TransactionRepository transactionRepository;

   

    // ─────────────────────────────────────────────────────────────────────────
    // Core calculation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calculates reward points for a single transaction amount.
     *
     * @param amount purchase amount in dollars
     * @return reward points earned
     */
    public long calculatePoints(BigDecimal amount) {
        long points = 0;

        if (amount.intValue() > 100) {
            points += (long) ((amount.intValue() - 100) * 2); // 2 pts per dollar over $100
            points += 50;                           // 1 pt per dollar between $50-$100 (fixed 50 pts)
        } else if (amount.intValue() > 50) {
            points += (long) (amount.intValue() - 50);         // 1 pt per dollar over $50
        }
        // $0–$50: 0 points

        return points;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // All customers summary
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns reward summaries for all customers.
     */
    public List<CustomerRewardSummary> getAllCustomerRewards() {
    	log.debug("Attempting to fetch customers rewards");
        List<Customer> customers = customerRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();

        return customers.stream()
                .map(customer -> buildSummary(customer, transactions))
                .sorted(Comparator.comparing(CustomerRewardSummary::getCustomerId))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Single customer summary
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the reward summary for a specific customer by ID.
     *
     * @param customerId the customer's ID (must be a positive number)
     * @return  CustomerRewardSummary for the requested customer
     * @throws InvalidCustomerIdException if  customerId is null, zero, or negative
     * @throws CustomerNotFoundException  if no customer exists with the given ID
     */
    public CustomerRewardSummary getCustomerRewards(Long customerId) {

    	log.debug("Attempting to fetch rewards with Customer ID: {}", customerId);
        // Validate input
        if (customerId == null || customerId <= 0) {
            throw new InvalidCustomerIdException(String.valueOf(customerId));
        }

        List<Customer> customers = customerRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();

        Customer customer = customers.stream()
                .filter(c -> c.getCustomerId().equals(customerId))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        return buildSummary(customer, transactions);
    }
    
    /**
     * Returns reward summaries for all customers within a date range.
     *
     * @param startDate start of date range (inclusive)
     * @param endDate   end of date range (inclusive)
     * @return List of CustomerRewardSummary within the date range
     */
	public List<CustomerRewardSummary> getCustomerRewardsByTransactionDateRange(LocalDate startDate,
			LocalDate endDate) {

		log.debug("Attempting to fetch rewards between Transaction data range: {} and {}", startDate, endDate);
		// Validate input
		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Start date and end date are required");
		}

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date must be before end date");
		}

		List<Customer> customers = customerRepository.findAll();
		List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(startDate, endDate);

		return customers.stream()
                .map(customer -> buildSummary(customer, transactions))
                .sorted(Comparator.comparing(CustomerRewardSummary::getCustomerId))
                .collect(Collectors.toList());
		
	}


    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a CustomerRewardSummary for the given customer using the
     * provided transaction list.
     */
    private CustomerRewardSummary buildSummary(Customer customer, List<Transaction> allTransactions) {

        // Filter transactions belonging to this customer
        List<Transaction> customerTxns = allTransactions.stream()
                .filter(t -> t.getCustomer().getCustomerId().equals(customer.getCustomerId()))
                .collect(Collectors.toList());

        // Group by year-month and sum points
        Map<String, Long> pointsByYearMonth = customerTxns.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().getYear() + "-" +
                             String.format("%02d", t.getTransactionDate().getMonthValue()),
                        Collectors.summingLong(t -> calculatePoints(t.getAmount()))
                ));

        // Build sorted monthly reward list
        List<MonthlyReward> monthlyRewards = new ArrayList<>();
        for (Map.Entry<String, Long> entry : pointsByYearMonth.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            monthlyRewards.add(new MonthlyReward(year, month, monthName, entry.getValue()));
        }

        // Sort by year then month
        monthlyRewards.sort(Comparator.comparingInt(MonthlyReward::getYear)
                                      .thenComparingInt(MonthlyReward::getMonth));

        long totalPoints = monthlyRewards.stream()
                .mapToLong(MonthlyReward::getPoints)
                .sum();

        return new CustomerRewardSummary(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                monthlyRewards,
                totalPoints
        );
    }
   
    /**
     * Persists a new customer along with their transactions.
     * If any operation fails, ALL changes are rolled back (atomicity).
     *
     * @param customerTransaction the customer and transaction data to persist
     * @return the persisted CustomerTransaction with saved data
     */
    @Transactional
    public CustomerTransaction persistCustomerTransaction(CustomerTransaction customerTransaction) {
    	log.info("Persisting customer transaction for: {}",
                customerTransaction.getCustomerDto().getCustomerEmail());
    	  if (customerTransaction.getCustomerDto() == null) {
              throw new IllegalArgumentException("Customer details are required");
          }

         if (customerTransaction.getTransactionsDto() == null
                  || customerTransaction.getTransactionsDto().isEmpty()) {
              throw new IllegalArgumentException(
                      "At least one transaction is required");
          }
    	Customer customer=new Customer();
    	customer.setEmail(customerTransaction.getCustomerDto().getCustomerEmail());
    	customer.setName(customerTransaction.getCustomerDto().getCustomerName());
    	Customer savedCustomer =customerRepository.save(customer);
    	log.info("Customer saved with ID: {}", savedCustomer.getCustomerId());
    	CustomerDto responseCustomerDto = new CustomerDto();
        responseCustomerDto.setCustomerName(savedCustomer.getName());
        responseCustomerDto.setCustomerEmail(savedCustomer.getEmail());
        List<TransactionDto> responseTransactions = new ArrayList<>();
    	for(TransactionDto txnDto:customerTransaction.getTransactionsDto()) {
    		if (txnDto.getAmount() == null || txnDto.getAmount().isBlank()) {
                throw new IllegalArgumentException(
                        "Transaction amount cannot be null or blank");
            }
            if (txnDto.getTransactionDate() == null
                    || txnDto.getTransactionDate().isBlank()) {
                throw new IllegalArgumentException(
                        "Transaction date cannot be null or blank");
            }
            BigDecimal amount;
            try {
                amount = new BigDecimal(txnDto.getAmount());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Transaction amount must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid transaction amount format: " + txnDto.getAmount());
            }

            
            LocalDate transactionDate;
            try {
                transactionDate = LocalDate.parse(txnDto.getTransactionDate());
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid transaction date format: "
                        + txnDto.getTransactionDate()
                        + ". Expected format: yyyy-MM-dd");
            }
    		Transaction transaction = new Transaction();
    		 transaction.setAmount(amount);
             transaction.setTransactionDate(transactionDate);
             transaction.setCustomer(savedCustomer);
             Transaction savedTxn = transactionRepository.save(transaction);
             log.info("Transaction saved with ID: {}", savedTxn.getTransactionId());
             TransactionDto responseDto = new TransactionDto();
             responseDto.setAmount(savedTxn.getAmount().toString());
             responseDto.setTransactionDate(savedTxn.getTransactionDate().toString());
             responseTransactions.add(responseDto);
    	}
    	CustomerTransaction response = new CustomerTransaction();
        response.setCustomerDto(responseCustomerDto);
        response.setTransactionsDto(responseTransactions);

        log.info("Successfully persisted customer ID: {} with {} transactions",
                savedCustomer.getCustomerId(), responseTransactions.size());

        return response;
    }
}
