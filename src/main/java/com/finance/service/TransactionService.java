package com.finance.service;

import com.finance.dto.TransactionListResponse;
import com.finance.dto.TransactionRequest;
import com.finance.dto.TransactionResponse;
import com.finance.entity.Category;
import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.ForbiddenException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService implements CategoryService.CategoryUsageChecker {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
    }

    @Override
    public boolean isCategoryInUse(Category category, User user) {
        return transactionRepository.existsByCategoryAndUser(category, user);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be a positive decimal value");
        }
        if (request.getDate() == null) {
            throw new BadRequestException("Transaction date is required");
        }
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new BadRequestException("Transaction category is required");
        }

        Category category = categoryService.findCategoryByNameForUser(request.getCategory(), user);

        Transaction transaction = new Transaction(
                request.getAmount(),
                request.getDate(),
                request.getDescription(),
                category,
                user
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(User user, LocalDate startDate, LocalDate endDate, String categoryName) {
        List<Transaction> transactions = transactionRepository.findFiltered(user, startDate, endDate, categoryName);
        List<TransactionResponse> responses = transactions.stream()
                .map(this::mapToResponse)
                .toList();
        return new TransactionListResponse(responses);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to transaction id: " + id);
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Transaction amount must be positive");
            }
            transaction.setAmount(request.getAmount());
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            Category category = categoryService.findCategoryByNameForUser(request.getCategory(), user);
            transaction.setCategory(category);
        }

        // NOTE: Date field cannot be updated per specification! Any provided date is ignored.

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to transaction id: " + id);
        }

        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getDate(),
                t.getCategory().getName(),
                t.getDescription(),
                t.getCategory().getType().name()
        );
    }
}
