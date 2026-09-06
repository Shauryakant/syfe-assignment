package com.finance.service;

import com.finance.dto.SavingsGoalListResponse;
import com.finance.dto.SavingsGoalRequest;
import com.finance.dto.SavingsGoalResponse;
import com.finance.entity.Category;
import com.finance.entity.CategoryType;
import com.finance.entity.SavingsGoal;
import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.ForbiddenException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.SavingsGoalRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "John", "123");
        testUser.setId(1L);

        otherUser = new User("other@example.com", "pass", "Other", "456");
        otherUser.setId(2L);
    }

    @Test
    void testCreateGoal_Success() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        SavingsGoalRequest request = new SavingsGoalRequest("Emergency Fund", new BigDecimal("5000.00"), futureDate, startDate);

        SavingsGoal goal = new SavingsGoal("Emergency Fund", new BigDecimal("5000.00"), futureDate, startDate, testUser);
        goal.setId(1L);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);

        Category salaryCat = new Category("Salary", CategoryType.INCOME, false, null);
        Transaction t1 = new Transaction(new BigDecimal("1000.00"), LocalDate.of(2024, 1, 15), "Sal", salaryCat, testUser);
        when(transactionRepository.findByUserAndDateGreaterThanEqual(testUser, startDate)).thenReturn(List.of(t1));

        SavingsGoalResponse response = savingsGoalService.createGoal(request, testUser);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("1000.00"), response.getCurrentProgress());
        assertEquals(20.0, response.getProgressPercentage());
        assertEquals(new BigDecimal("4000.00"), response.getRemainingAmount());
    }

    @Test
    void testCreateGoal_PastTargetDate() {
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        SavingsGoalRequest request = new SavingsGoalRequest("Emergency Fund", new BigDecimal("5000.00"), pastDate, null);

        assertThrows(BadRequestException.class, () -> savingsGoalService.createGoal(request, testUser));
    }

    @Test
    void testCreateGoal_NegativeAmount() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoalRequest request = new SavingsGoalRequest("Emergency Fund", new BigDecimal("-5000.00"), futureDate, null);

        assertThrows(BadRequestException.class, () -> savingsGoalService.createGoal(request, testUser));
    }

    @Test
    void testGetGoalById_Success() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoal goal = new SavingsGoal("Vacation", new BigDecimal("2000.00"), futureDate, LocalDate.now(), testUser);
        goal.setId(1L);

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        SavingsGoalResponse response = savingsGoalService.getGoalById(1L, testUser);

        assertNotNull(response);
        assertEquals("Vacation", response.getGoalName());
    }

    @Test
    void testGetGoalById_Forbidden() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoal goal = new SavingsGoal("Vacation", new BigDecimal("2000.00"), futureDate, LocalDate.now(), otherUser);
        goal.setId(1L);

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class, () -> savingsGoalService.getGoalById(1L, testUser));
    }

    @Test
    void testUpdateGoal_Success() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoal goal = new SavingsGoal("Vacation", new BigDecimal("2000.00"), futureDate, LocalDate.now(), testUser);
        goal.setId(1L);

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(i -> i.getArgument(0));

        SavingsGoalRequest updateReq = new SavingsGoalRequest("New Vacation", new BigDecimal("3000.00"), null, null);

        SavingsGoalResponse response = savingsGoalService.updateGoal(1L, updateReq, testUser);

        assertNotNull(response);
        assertEquals("New Vacation", response.getGoalName());
        assertEquals(new BigDecimal("3000.00"), response.getTargetAmount());
    }

    @Test
    void testDeleteGoal_Success() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        SavingsGoal goal = new SavingsGoal("Vacation", new BigDecimal("2000.00"), futureDate, LocalDate.now(), testUser);
        goal.setId(1L);

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        savingsGoalService.deleteGoal(1L, testUser);

        verify(savingsGoalRepository, times(1)).delete(goal);
    }
}
