package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.SavingsGoalListResponse;
import com.finance.dto.SavingsGoalRequest;
import com.finance.dto.SavingsGoalResponse;
import com.finance.entity.User;
import com.finance.service.SavingsGoalService;
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
class SavingsGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SavingsGoalService savingsGoalService;

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
    void testCreateGoal_Success() throws Exception {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoalRequest request = new SavingsGoalRequest("Emergency", new BigDecimal("5000.00"), futureDate, LocalDate.of(2024, 1, 1));
        SavingsGoalResponse response = new SavingsGoalResponse(1L, "Emergency", new BigDecimal("5000.00"), futureDate, LocalDate.of(2024, 1, 1), new BigDecimal("1000.00"), 20.0, new BigDecimal("4000.00"));

        when(savingsGoalService.createGoal(any(SavingsGoalRequest.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalName").value("Emergency"))
                .andExpect(jsonPath("$.currentProgress").value(1000.00));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetAllGoals_Success() throws Exception {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoalResponse response = new SavingsGoalResponse(1L, "Emergency", new BigDecimal("5000.00"), futureDate, LocalDate.of(2024, 1, 1), new BigDecimal("1000.00"), 20.0, new BigDecimal("4000.00"));
        SavingsGoalListResponse listResponse = new SavingsGoalListResponse(List.of(response));

        when(savingsGoalService.getAllGoals(testUser)).thenReturn(listResponse);

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetGoalById_Success() throws Exception {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoalResponse response = new SavingsGoalResponse(1L, "Emergency", new BigDecimal("5000.00"), futureDate, LocalDate.of(2024, 1, 1), new BigDecimal("1000.00"), 20.0, new BigDecimal("4000.00"));

        when(savingsGoalService.getGoalById(1L, testUser)).thenReturn(response);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testUpdateGoal_Success() throws Exception {
        SavingsGoalRequest request = new SavingsGoalRequest(null, new BigDecimal("6000.00"), null, null);
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoalResponse response = new SavingsGoalResponse(1L, "Emergency", new BigDecimal("6000.00"), futureDate, LocalDate.of(2024, 1, 1), new BigDecimal("1000.00"), 16.67, new BigDecimal("5000.00"));

        when(savingsGoalService.updateGoal(eq(1L), any(SavingsGoalRequest.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(6000.00));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testDeleteGoal_Success() throws Exception {
        doNothing().when(savingsGoalService).deleteGoal(eq(1L), eq(testUser));

        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }
}
