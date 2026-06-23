package com.retailer.reward.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailer.reward.dto.CustomerDto;
import com.retailer.reward.dto.CustomerRewardSummary;
import com.retailer.reward.dto.CustomerTransaction;
import com.retailer.reward.dto.TransactionDto;
import com.retailer.reward.service.RewardService;

@WebMvcTest(RewardController.class)
@DisplayName("Reward Controller Tests")
public class RewardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RewardService rewardService;

	@Autowired
	private ObjectMapper objectMapper;

	private CustomerTransaction customerTransaction;

	private TransactionDto monthlyTransaction1;

	private TransactionDto monthlyTransaction2;

	private List<TransactionDto> monthlyTransactions;

	CustomerRewardSummary customerRewardSummary1;
	
	CustomerDto customerDto;

	@BeforeEach
	void setUp() {
		// Initialize test data
		monthlyTransactions = new ArrayList<TransactionDto>();
		customerTransaction = new CustomerTransaction();
		monthlyTransaction1 = new TransactionDto();
		monthlyTransaction2 = new TransactionDto();
		customerDto=new CustomerDto();
		customerTransaction.setCustomerDto(customerDto);
		customerTransaction.getCustomerDto().setCustomerEmail("TestOne@example.com");
		customerTransaction.getCustomerDto().setCustomerName("Test One");
		monthlyTransaction1.setAmount("120");
		monthlyTransaction1.setTransactionDate("2026-03-05");
		monthlyTransaction2.setAmount("75");
		monthlyTransaction2.setTransactionDate("2026-03-14");
		monthlyTransactions.add(monthlyTransaction1);
		monthlyTransactions.add(monthlyTransaction2);
		customerTransaction.setTransactionsDto(monthlyTransactions);
		customerRewardSummary1 = new CustomerRewardSummary();
		customerRewardSummary1.setCustomerId(1L);
		customerRewardSummary1.setCustomerEmail("TestOne@example.com");
		customerRewardSummary1.setTotalPoints(150);
	}

	// ==================== CREATE Customer Transacction ====================

	@Test
	@DisplayName("Should create customer transaction successfully")
	void testCreateCustomerTransactionSuccess() throws Exception {
		// Arrange
		given(rewardService.persistCustomerTransaction(any(CustomerTransaction.class))).willReturn(customerTransaction);

		// Act
		ResultActions response = mockMvc.perform(post("/api/rewards").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerTransaction)));

		// Assert
		response.andDo(print()).andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerDto.customerName",
						is(customerTransaction.getCustomerDto().getCustomerName())))
				.andExpect(jsonPath("$.customerDto.customerEmail",
						is(customerTransaction.getCustomerDto().getCustomerEmail())));

		verify(rewardService, times(1)).persistCustomerTransaction(any(CustomerTransaction.class));
	}

	@Test
	@DisplayName("Should return 500 when CustomerRewards not found by ID")
	void testGetCustomerRewardsByCustomerIdNotFound() throws Exception {
		// Arrange
		given(rewardService.getCustomerRewards(anyLong())).willThrow(new RuntimeException("CustomerRewards not found"));

		// Act
		ResultActions response = mockMvc
				.perform(get("/api/rewards/{customerId}", 999L).contentType(MediaType.APPLICATION_JSON));

		// Assert
		response.andDo(print()).andExpect(status().isInternalServerError());

		verify(rewardService, times(1)).getCustomerRewards(anyLong());
	}

	@Test
	@DisplayName("Get rewards for all customer and return 200 OK")
	void testGetAllRewards() throws Exception {
		// Arrange
		CustomerRewardSummary customerRewardSummary2 = new CustomerRewardSummary();
		customerRewardSummary2.setCustomerId(2L);
		customerRewardSummary2.setCustomerEmail("test.two@example.com");
		customerRewardSummary2.setTotalPoints(250);

		List<CustomerRewardSummary> customerRewardSummarys = Arrays.asList(customerRewardSummary1,
				customerRewardSummary2);
		given(rewardService.getAllCustomerRewards()).willReturn(customerRewardSummarys);

		// Act
		ResultActions response = mockMvc.perform(get("/api/rewards").contentType(MediaType.APPLICATION_JSON));

		// Assert
		response.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].customerId", is(customerRewardSummary1.getCustomerId().intValue())))
				.andExpect(jsonPath("$[0].customerEmail", is(customerRewardSummary1.getCustomerEmail())))
				.andExpect(jsonPath("$[1].customerId", is(customerRewardSummary2.getCustomerId().intValue())))
				.andExpect(jsonPath("$[1].customerEmail", is(customerRewardSummary2.getCustomerEmail())));

		verify(rewardService, times(1)).getAllCustomerRewards();
	}

	@Test
	@DisplayName("Should return empty list when no customer reward exist")
	void testGetAllRewardsEmpty() throws Exception {
		// Arrange
		given(rewardService.getAllCustomerRewards()).willReturn(Arrays.asList());

		// Act
		ResultActions response = mockMvc.perform(get("/api/rewards").contentType(MediaType.APPLICATION_JSON));

		// Assert
		response.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

		verify(rewardService, times(1)).getAllCustomerRewards();
	}

	@Test
	@DisplayName("Should get customer rewards by date range")
	void testGetCustomerRewardByDateRange() throws Exception {
		// Arrange

		CustomerRewardSummary customerRewardSummary2 = new CustomerRewardSummary();
		customerRewardSummary2.setCustomerId(2L);
		customerRewardSummary2.setCustomerEmail("test.two@example.com");
		customerRewardSummary2.setTotalPoints(250);
		monthlyTransaction1.setAmount("120");
		monthlyTransaction1.setTransactionDate("2026-03-05");
		monthlyTransaction2.setAmount("75");
		monthlyTransaction2.setTransactionDate("2026-03-14");
		List<CustomerRewardSummary> customerRewardSummaryList = Arrays.asList(customerRewardSummary1,
				customerRewardSummary2);

		given(rewardService.getCustomerRewardsByTransactionDateRange(any(LocalDate.class), any(LocalDate.class)))
				.willReturn(customerRewardSummaryList);

		// Act
		ResultActions result = mockMvc
				.perform(get("/api/rewards/date-range").param("startDate", "2026-03-06")
						.param("endDate", "2026-03-15").contentType(MediaType.APPLICATION_JSON));

		// Assert
		result.andDo(print()).andExpect(status().isOk());

		verify(rewardService, times(1)).getCustomerRewardsByTransactionDateRange(any(LocalDate.class),
				any(LocalDate.class));

	}

}
