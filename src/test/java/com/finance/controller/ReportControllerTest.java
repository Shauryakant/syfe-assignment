package com.finance.controller;

import com.finance.dto.MonthlyReportResponse;
import com.finance.dto.YearlyReportResponse;
import com.finance.entity.User;
import com.finance.service.ReportService;
import com.finance.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "John", "123");
        testUser.setId(1L);
        when(userService.getUserByUsername("user@example.com")).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetMonthlyReport_Success() throws Exception {
        MonthlyReportResponse response = new MonthlyReportResponse(1, 2024, Map.of("Salary", new BigDecimal("3000.00")), Map.of("Food", new BigDecimal("400.00")), new BigDecimal("2600.00"));
        when(reportService.generateMonthlyReport(eq(2024), eq(1), eq(testUser))).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.netSavings").value(2600.00));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetYearlyReport_Success() throws Exception {
        YearlyReportResponse response = new YearlyReportResponse(2024, Map.of("Salary", new BigDecimal("36000.00")), Map.of("Food", new BigDecimal("4800.00")), new BigDecimal("31200.00"));
        when(reportService.generateYearlyReport(eq(2024), eq(testUser))).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.netSavings").value(31200.00));
    }

    @Test
    void testReports_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isUnauthorized());
    }
}
