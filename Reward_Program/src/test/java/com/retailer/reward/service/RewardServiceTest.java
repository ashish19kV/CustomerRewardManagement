package com.retailer.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.retailer.reward.dto.CustomerDto;
import com.retailer.reward.dto.CustomerRewardSummary;
import com.retailer.reward.dto.CustomerTransaction;
import com.retailer.reward.dto.TransactionDto;
import com.retailer.reward.exception.CustomerNotFoundException;
import com.retailer.reward.exception.InvalidCustomerIdException;
import com.retailer.reward.model.Customer;
import com.retailer.reward.model.Transaction;
import com.retailer.reward.repository.CustomerRepository;
import com.retailer.reward.repository.TransactionRepository;



/**
 * Unit tests for RewardService
 *
 * Tests cover:
 *  1. Point calculation edge cases
 *  2. All-customer summary
 *  3. Single-customer lookup (found / not found / invalid ID)
 *  4. Exception handling
 *  5. Date Range summary
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Reward Service Tests")
class RewardServiceTest {

	@Mock
    private CustomerRepository customerRepository;
	
	@Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardService rewardService;
    
    private CustomerTransaction customerTransaction;
    
    private TransactionDto transactionDto;
    
    private CustomerDto customerDto;
    
    private Customer customer;
    
    private Transaction transaction;
    
    @BeforeEach
    void setUp() {
    	customer=new Customer();
    	transaction=new Transaction();
    	customer.setCustomerId(1L);
    	customer.setEmail("test.one@example.com");
    	customer.setName("Test One");
    	BigDecimal bigDecimal=new BigDecimal("120");
    	transaction.setAmount(bigDecimal);
    	transaction.setTransactionId(1L);
    	LocalDate transactionDate=LocalDate.parse("2026-03-07");
    	transaction.setTransactionDate(transactionDate);
    	transaction.setCustomer(customer);
    	customerTransaction=new CustomerTransaction();
    	if(customerTransaction.getTransactionsDto()==null) {
          	List<TransactionDto> transactionDtoList=new ArrayList<TransactionDto>();
          	customerTransaction.setTransactionsDto(transactionDtoList);
          }
    	customerDto=new CustomerDto();
    	transactionDto=new TransactionDto();
    	customerDto.setCustomerEmail("test.one@example.com");
    	customerDto.setCustomerName("Test One");
    	transactionDto.setAmount("120");
    	transactionDto.setTransactionDate("2026-03-07");
    	customerTransaction.setCustomerDto(customerDto);
        customerTransaction.getTransactionsDto().add(transactionDto);
    	
    }

    @Test
    @DisplayName("Should create Customer and Transaction successfully")
    void testCreateCustomerTransactionSuccessfully() {
    	given(customerRepository.save(any(Customer.class)))
        .willReturn(customer);
    	given(transactionRepository.save(any(Transaction.class)))
        .willReturn(transaction);

    	
        // Act
       CustomerTransaction result=rewardService.persistCustomerTransaction(customerTransaction);
       assertThat(result)
       .isNotNull()
       .extracting("customerDto.customerName")
       .isEqualTo("Test One");
    }
    
    
    
    // ─────────────────────────────────────────────────────────────────────────
    // calculatePoints() – boundary / edge cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Amount $0 → 0 points")
    void testZeroAmount() {
    	BigDecimal bigDecimal = new BigDecimal("0");
        assertEquals(0, rewardService.calculatePoints(bigDecimal));
    }

    @Test
    @DisplayName("Amount $49.99 (below $50 threshold) → 0 points")
    void testBelowFiftyThreshold() {
    	BigDecimal bigDecimal = new BigDecimal("49.99");
        assertEquals(0, rewardService.calculatePoints(bigDecimal));
    }

    @Test
    @DisplayName("Amount exactly $50 → 0 points (boundary)")
    void testExactlyFifty() {
    	BigDecimal bigDecimal = new BigDecimal("50");
        assertEquals(0, rewardService.calculatePoints(bigDecimal));
    }

    @Test
    @DisplayName("Amount $75 → 25 points  (1 × $25 over $50)")
    void testBetweenFiftyAndHundred() {
    	BigDecimal bigDecimal = new BigDecimal("75");
        assertEquals(25, rewardService.calculatePoints(bigDecimal));
    }

    @Test
    @DisplayName("Amount exactly $100 → 50 points (boundary)")
    void testExactlyHundred() {
    	BigDecimal bigDecimal = new BigDecimal("100");
        assertEquals(50, rewardService.calculatePoints(bigDecimal));
    }

    @Test
    @DisplayName("Amount $120 → 90 points  (2×$20 + 1×$50 = 90)")
    void testAboveHundred() {
    	BigDecimal bigDecimal = new BigDecimal("120");
        assertEquals(90, rewardService.calculatePoints(bigDecimal));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllCustomerRewards()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("All customers: returns 2 summaries")
    void testAllCustomersRewardCount() {
    	Customer customer2=new Customer();
    	Transaction transaction2=new Transaction();
    	customer2.setCustomerId(2L);
    	customer2.setEmail("test.two@example.com");
    	customer2.setName("Test two");
    	BigDecimal bigDecimal2=new BigDecimal("90");
    	transaction2.setAmount(bigDecimal2);
    	transaction2.setTransactionId(2L);
    	LocalDate transactionDate2=LocalDate.parse("2026-03-15");
    	transaction2.setTransactionDate(transactionDate2);
    	transaction2.setCustomer(customer2);
    	List<Customer> customers = Arrays.asList(customer, customer2);
    	List<Transaction> transactions = Arrays.asList(transaction, transaction2);
    	given(transactionRepository.findAll()).willReturn(transactions);
    	given(customerRepository.findAll()).willReturn(customers);
    	
        List<CustomerRewardSummary> summaries = rewardService.getAllCustomerRewards();
        assertEquals(2, summaries.size());
    }
    
    

    @Test
    @DisplayName("All customers: each summary has atleast 1 monthly entries")
    void testAllCustomersHaveThreeMonths() {
    	Customer customer2=new Customer();
    	Transaction transaction2=new Transaction();
    	customer2.setCustomerId(2L);
    	customer2.setEmail("test.two@example.com");
    	customer2.setName("Test two");
    	BigDecimal bigDecimal2=new BigDecimal("90");
    	transaction2.setAmount(bigDecimal2);
    	transaction2.setTransactionId(2L);
    	LocalDate transactionDate2=LocalDate.parse("2026-04-15");
    	transaction2.setTransactionDate(transactionDate2);
    	transaction2.setCustomer(customer2);
    	Customer customer3=new Customer();
    	Transaction transaction3=new Transaction();
    	customer3.setCustomerId(3L);
    	customer3.setEmail("test.three@example.com");
    	customer3.setName("Test three");
    	BigDecimal bigDecimal3=new BigDecimal("255");
    	transaction3.setAmount(bigDecimal3);
    	transaction3.setTransactionId(3L);
    	LocalDate transactionDate3=LocalDate.parse("2026-05-04");
    	transaction3.setTransactionDate(transactionDate3);
    	transaction3.setCustomer(customer3);

    	List<Customer> customers = Arrays.asList(customer, customer2,customer3);
    	List<Transaction> transactions = Arrays.asList(transaction, transaction2,transaction3);
    	given(transactionRepository.findAll()).willReturn(transactions);
    	given(customerRepository.findAll()).willReturn(customers);
    	
        List<CustomerRewardSummary> summaries = rewardService.getAllCustomerRewards();
        for (CustomerRewardSummary summary : summaries) {
            assertEquals(1, summary.getMonthlyRewards().size(),
                    "Customer " + summary.getCustomerName() + " should have atleast 1 monthly entries");
        }
    }

    
    // ─────────────────────────────────────────────────────────────────────────
    // getCustomerRewards() – single customer 
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Customer 1 (Test User1): found and total points = 670")
    void testCustomerOneTotalPoints() {
    	Customer customer2=new Customer();
    	Transaction transaction2=new Transaction();
    	customer2.setCustomerId(2L);
    	customer2.setEmail("test.two@example.com");
    	customer2.setName("Test two");
    	BigDecimal bigDecimal2=new BigDecimal("90");
    	transaction2.setAmount(bigDecimal2);
    	transaction2.setTransactionId(2L);
    	LocalDate transactionDate2=LocalDate.parse("2026-03-15");
    	transaction2.setTransactionDate(transactionDate2);
    	transaction2.setCustomer(customer2);
    	List<Customer> customers = Arrays.asList(customer, customer2);
    	List<Transaction> transactions = Arrays.asList(transaction, transaction2);
    	given(transactionRepository.findAll()).willReturn(transactions);
    	given(customerRepository.findAll()).willReturn(customers);

        CustomerRewardSummary result = rewardService.getCustomerRewards(1L);
        assertNotNull(result);
        assertEquals("Test One", result.getCustomerName());
        assertEquals(90, result.getTotalPoints());
    }

    @Test
    @DisplayName("Customer 2 (Test User2): found and total points = 595")
    void testCustomerTwoTotalPoints() {
    	Customer customer2=new Customer();
    	Transaction transaction2=new Transaction();
    	customer2.setCustomerId(2L);
    	customer2.setEmail("test.two@example.com");
    	customer2.setName("Test two");
    	BigDecimal bigDecimal2=new BigDecimal("90");
    	transaction2.setAmount(bigDecimal2);
    	transaction2.setTransactionId(2L);
    	LocalDate transactionDate2=LocalDate.parse("2026-03-15");
    	transaction2.setTransactionDate(transactionDate2);
    	transaction2.setCustomer(customer2);
    	List<Customer> customers = Arrays.asList(customer, customer2);
    	List<Transaction> transactions = Arrays.asList(transaction, transaction2);
    	given(transactionRepository.findAll()).willReturn(transactions);
    	given(customerRepository.findAll()).willReturn(customers);

        CustomerRewardSummary result = rewardService.getCustomerRewards(2L);
        assertNotNull(result);
        assertEquals("Test two", result.getCustomerName());
         assertEquals(40, result.getTotalPoints());
    }

    
    
    // ─────────────────────────────────────────────────────────────────────────
    // getCustomerRewards() – exception cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Non-existent customer (ID=999) → throws CustomerNotFoundException")
    void testCustomerNotFound() {
        CustomerNotFoundException ex = assertThrows(
                CustomerNotFoundException.class,
                () -> rewardService.getCustomerRewards(999L)
        );
        assertEquals(999L, ex.getCustomerId());
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("Zero customer ID → throws InvalidCustomerIdException")
    void testZeroCustomerId() {
        assertThrows(
                InvalidCustomerIdException.class,
                () -> rewardService.getCustomerRewards(0L)
        );
    }

    @Test
    @DisplayName("Negative customer ID → throws InvalidCustomerIdException")
    void testNegativeCustomerId() {
        InvalidCustomerIdException ex = assertThrows(
                InvalidCustomerIdException.class,
                () -> rewardService.getCustomerRewards(-5L)
        );
        assertTrue(ex.getMessage().contains("-5"));
    }

    @Test
    @DisplayName("Null customer ID → throws InvalidCustomerIdException")
    void testNullCustomerId() {
        assertThrows(
                InvalidCustomerIdException.class,
                () -> rewardService.getCustomerRewards(null)
        );
    }
    
    @Test
    @DisplayName("All customers within data range")
    void testAllCustomersRewardCountBasedOnDateRange() {
    	Customer customer2=new Customer();
    	Transaction transaction2=new Transaction();
    	customer2.setCustomerId(2L);
    	customer2.setEmail("test.two@example.com");
    	customer2.setName("Test two");
    	BigDecimal bigDecimal2=new BigDecimal("90.00");
    	transaction2.setAmount(bigDecimal2);
    	transaction2.setTransactionId(2L);
    	LocalDate transactionDate2=LocalDate.parse("2026-04-15");
    	transaction2.setTransactionDate(transactionDate2);
    	transaction2.setCustomer(customer2);
    	LocalDate start = LocalDate.of(2026, 03, 10);
        LocalDate end = LocalDate.of(2026, 04, 25);
    	List<Customer> customers = Arrays.asList(customer, customer2);
    	List<Transaction> transactions = Arrays.asList(transaction, transaction2);
    	given(transactionRepository.findByTransactionDateBetween(start,end)).willReturn(transactions);
    	given(customerRepository.findAll()).willReturn(customers);
    	
        List<CustomerRewardSummary> summaryList = rewardService.getCustomerRewardsByTransactionDateRange(start,end);
        assertTrue(summaryList.size() > 0);
    }
   
}
