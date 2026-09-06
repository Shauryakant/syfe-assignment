package com.finance.service;

import com.finance.dto.MonthlyReportResponse;
import com.finance.dto.YearlyReportResponse;
import com.finance.entity.Category;
import com.finance.entity.CategoryType;
import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "John", "123");
        testUser.setId(1L);
    }

    @Test
    void testGenerateMonthlyReport_Success() {
        Category salaryCat = new Category("Salary", CategoryType.INCOME, false, null);
        Category foodCat = new Category("Food", CategoryType.EXPENSE, false, null);

        Transaction t1 = new Transaction(new BigDecimal("3000.00"), LocalDate.of(2024, 1, 15), "Sal", salaryCat, testUser);
        Transaction t2 = new Transaction(new BigDecimal("400.00"), LocalDate.of(2024, 1, 16), "Food", foodCat, testUser);

        when(transactionRepository.findByUserAndDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        MonthlyReportResponse report = reportService.generateMonthlyReport(2024, 1, testUser);

        assertNotNull(report);
        assertEquals(1, report.getMonth());
        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("3000.00"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("400.00"), report.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("2600.00"), report.getNetSavings());
    }

    @Test
    void testGenerateMonthlyReport_InvalidMonth() {
        assertThrows(BadRequestException.class, () -> reportService.generateMonthlyReport(2024, 13, testUser));
        assertThrows(BadRequestException.class, () -> reportService.generateMonthlyReport(2024, 0, testUser));
    }

    @Test
    void testGenerateYearlyReport_Success() {
        Category salaryCat = new Category("Salary", CategoryType.INCOME, false, null);
        Transaction t1 = new Transaction(new BigDecimal("36000.00"), LocalDate.of(2024, 6, 15), "Sal", salaryCat, testUser);

        when(transactionRepository.findByUserAndDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1));

        YearlyReportResponse report = reportService.generateYearlyReport(2024, testUser);

        assertNotNull(report);
        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("36000.00"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("36000.00"), report.getNetSavings());
    }
}
