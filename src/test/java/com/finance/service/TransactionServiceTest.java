package com.finance.service;

import com.finance.dto.TransactionListResponse;
import com.finance.dto.TransactionRequest;
import com.finance.dto.TransactionResponse;
import com.finance.entity.Category;
import com.finance.entity.CategoryType;
import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.ForbiddenException;
import com.finance.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private User otherUser;
    private Category salaryCategory;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "John", "123");
        testUser.setId(1L);

        otherUser = new User("other@example.com", "pass", "Other", "456");
        otherUser.setId(2L);

        salaryCategory = new Category("Salary", CategoryType.INCOME, false, null);
    }

    @Test
    void testCreateTransaction_Success() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Salary", "January Salary");
        when(categoryService.findCategoryByNameForUser("Salary", testUser)).thenReturn(salaryCategory);

        Transaction saved = new Transaction(request.getAmount(), request.getDate(), request.getDescription(), salaryCategory, testUser);
        saved.setId(1L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction(request, testUser);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("INCOME", response.getType());
    }

    @Test
    void testCreateTransaction_FutureDate() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("100.00"), LocalDate.now().plusDays(10), "Salary", "Future");

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request, testUser));
    }

    @Test
    void testCreateTransaction_NegativeAmount() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("-100.00"), LocalDate.now(), "Salary", "Negative");

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request, testUser));
    }

    @Test
    void testGetTransactions_Filtered() {
        Transaction t1 = new Transaction(new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Sal", salaryCategory, testUser);
        t1.setId(1L);

        when(transactionRepository.findFiltered(testUser, null, null, null)).thenReturn(List.of(t1));

        TransactionListResponse response = transactionService.getTransactions(testUser, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
    }

    @Test
    void testUpdateTransaction_IgnoresDate() {
        Transaction t1 = new Transaction(new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Sal", salaryCategory, testUser);
        t1.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(categoryService.findCategoryByNameForUser("Salary", testUser)).thenReturn(salaryCategory);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionRequest updateReq = new TransactionRequest(new BigDecimal("6000.00"), LocalDate.of(2024, 1, 20), "Salary", "Updated Sal");

        TransactionResponse response = transactionService.updateTransaction(1L, updateReq, testUser);

        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), response.getAmount());
        assertEquals(LocalDate.of(2024, 1, 15), response.getDate()); // Date unchanged!
    }

    @Test
    void testUpdateTransaction_Forbidden() {
        Transaction t1 = new Transaction(new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Sal", salaryCategory, otherUser);
        t1.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t1));

        TransactionRequest updateReq = new TransactionRequest(new BigDecimal("6000.00"), null, null, null);

        assertThrows(ForbiddenException.class, () -> transactionService.updateTransaction(1L, updateReq, testUser));
    }

    @Test
    void testDeleteTransaction_Success() {
        Transaction t1 = new Transaction(new BigDecimal("5000.00"), LocalDate.of(2024, 1, 15), "Sal", salaryCategory, testUser);
        t1.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t1));

        transactionService.deleteTransaction(1L, testUser);

        verify(transactionRepository, times(1)).delete(t1);
    }
}
