package com.retailer.reward.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailer.reward.dto.CustomerDto;
import com.retailer.reward.dto.CustomerTransaction;
import com.retailer.reward.dto.TransactionDto;
import com.retailer.reward.repository.CustomerRepository;
import com.retailer.reward.service.RewardService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Reward Controller Integration Tests")
public class RewardControllerIntegrationTest {
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
    private CustomerRepository customerRepository;
	
	@Autowired
    private RewardService rewardService;
	
	private CustomerTransaction customerTransaction;
	
	private TransactionDto transactionDto;
	private CustomerDto customerDto;
	
	@BeforeEach
    void setUp() {
       customerTransaction=new CustomerTransaction();
        if(customerTransaction.getTransactionsDto()==null) {
        	List<TransactionDto> transactionDtoList=new ArrayList<TransactionDto>();
        	customerTransaction.setTransactionsDto(transactionDtoList);
        }
		transactionDto=new TransactionDto();
		customerDto=new CustomerDto();
		customerTransaction.setCustomerDto(customerDto);
		customerTransaction.getCustomerDto().setCustomerEmail("test.one@example.com");
		customerTransaction.getCustomerDto().setCustomerName("Test One");
		transactionDto.setAmount("120");
		transactionDto.setTransactionDate("2026-03-07");
        customerTransaction.getTransactionsDto().add(transactionDto);   
	}
	
    @Test
    @DisplayName("Should create CustomerTransaction through REST endpoint")
    @Transactional
    void testCreateCustomerTransactionThroughEndpoint() throws Exception {
        // Act
        ResultActions response = mockMvc.perform(post("/api/rewards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(customerTransaction)));

        // Assert
        response.andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerDto.customerName", is(customerTransaction.getCustomerDto().getCustomerName())))
            .andExpect(jsonPath("$.customerDto.customerEmail", is(customerTransaction.getCustomerDto().getCustomerEmail())));
           

        // Verify in database
        assert customerRepository.findAll().size()>0;
    }

    
    @Test
    @DisplayName("Should retrieve customer reward by customerId through REST endpoint")
    @Transactional
    void testGetCustomerRewardByCustomerIdThroughEndpoint() throws Exception {
        // Arrange
       CustomerTransaction customerTransactionObj = rewardService.persistCustomerTransaction(customerTransaction);

        // Act
        ResultActions response = mockMvc.perform(get("/api/rewards/{customerId}", 4L)
            .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerName", is(customerTransactionObj.getCustomerDto().getCustomerName())));
    }

    @Test
    @DisplayName("Should retrieve all customer rewards through REST endpoint")
    @Transactional
    void testGetAllCustomerRewardsThroughEndpoint() throws Exception {
        // Arrange
    	rewardService.persistCustomerTransaction(customerTransaction);
    	CustomerTransaction customerTransaction1=new CustomerTransaction();
    	  if(customerTransaction1.getTransactionsDto()==null) {
          	List<TransactionDto> transactionDtoList=new ArrayList<TransactionDto>();
          	customerTransaction1.setTransactionsDto(transactionDtoList);
          }
  		transactionDto=new TransactionDto();
  		customerDto=new CustomerDto();
  		customerTransaction1.setCustomerDto(customerDto);
    	TransactionDto monthlyTransaction1=new TransactionDto();
		customerTransaction1.getCustomerDto().setCustomerEmail("test.two@example.com");
		customerTransaction1.getCustomerDto().setCustomerName("Test Two");
		monthlyTransaction1.setAmount("230");
		monthlyTransaction1.setTransactionDate("2026-04-07");
		customerTransaction1.getTransactionsDto().add(monthlyTransaction1); 
        rewardService.persistCustomerTransaction(customerTransaction1);

        // Act
        ResultActions response = mockMvc.perform(get("/api/rewards")
            .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }



}
