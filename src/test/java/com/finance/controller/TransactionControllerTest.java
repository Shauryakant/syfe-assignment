package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.TransactionListResponse;
import com.finance.dto.TransactionRequest;
import com.finance.dto.TransactionResponse;
import com.finance.entity.User;
import com.finance.service.TransactionService;
import com.finance.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "John", "123");
        testUser.setId(1L);
        when(userService.getUserByUsername("user@example.com")).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testCreateTransaction_Success() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Salary", "Jan Sal");
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Salary", "Jan Sal", "INCOME");

        when(transactionService.createTransaction(any(TransactionRequest.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetTransactions_Success() throws Exception {
        TransactionResponse res1 = new TransactionResponse(1L, new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Salary", "Jan Sal", "INCOME");
        TransactionListResponse response = new TransactionListResponse(List.of(res1));

        when(transactionService.getTransactions(eq(testUser), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testUpdateTransaction_Success() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal("6000.00"), null, null, "Updated");
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("6000.00"), LocalDate.of(2024, 1, 15), "Salary", "Updated", "INCOME");

        when(transactionService.updateTransaction(eq(1L), any(TransactionRequest.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(6000.00));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testDeleteTransaction_Success() throws Exception {
        doNothing().when(transactionService).deleteTransaction(eq(1L), eq(testUser));

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }

    @Test
    void testTransactions_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }
}
