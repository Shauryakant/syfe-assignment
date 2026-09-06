package com.finance.service;

import com.finance.dto.SavingsGoalListResponse;
import com.finance.dto.SavingsGoalRequest;
import com.finance.dto.SavingsGoalResponse;
import com.finance.entity.CategoryType;
import com.finance.entity.SavingsGoal;
import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.ForbiddenException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.SavingsGoalRepository;
import com.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, TransactionRepository transactionRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request, User user) {
        if (request.getGoalName() == null || request.getGoalName().isBlank()) {
            throw new BadRequestException("Goal name is required");
        }
        if (request.getTargetAmount() == null || request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target amount must be a positive decimal value");
        }
        if (request.getTargetDate() == null) {
            throw new BadRequestException("Target date is required");
        }
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        if (startDate.isAfter(request.getTargetDate())) {
            throw new BadRequestException("Start date cannot be after target date");
        }

        SavingsGoal goal = new SavingsGoal(
                request.getGoalName(),
                request.getTargetAmount(),
                request.getTargetDate(),
                startDate,
                user
        );

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return calculateAndMapResponse(saved, user);
    }

    @Transactional(readOnly = true)
    public SavingsGoalListResponse getAllGoals(User user) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUser(user);
        List<SavingsGoalResponse> responses = goals.stream()
                .map(g -> calculateAndMapResponse(g, user))
                .toList();
        return new SavingsGoalListResponse(responses);
    }

    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoalById(Long id, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to goal id: " + id);
        }

        return calculateAndMapResponse(goal, user);
    }

    @Transactional
    public SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to goal id: " + id);
        }

        if (request.getGoalName() != null && !request.getGoalName().isBlank()) {
            goal.setGoalName(request.getGoalName());
        }

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target amount must be a positive decimal value");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        if (request.getStartDate() != null) {
            if (request.getStartDate().isAfter(goal.getTargetDate())) {
                throw new BadRequestException("Start date cannot be after target date");
            }
            goal.setStartDate(request.getStartDate());
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return calculateAndMapResponse(updated, user);
    }

    @Transactional
    public void deleteGoal(Long id, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to goal id: " + id);
        }

        savingsGoalRepository.delete(goal);
    }

    private SavingsGoalResponse calculateAndMapResponse(SavingsGoal goal, User user) {
        List<Transaction> transactions = transactionRepository.findByUserAndDateGreaterThanEqual(user, goal.getStartDate());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getCategory().getType() == CategoryType.EXPENSE) {
                totalExpenses = totalExpenses.add(t.getAmount());
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        BigDecimal currentProgress = netSavings.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : netSavings.setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            remainingAmount = BigDecimal.ZERO;
        } else {
            remainingAmount = remainingAmount.setScale(2, RoundingMode.HALF_UP);
        }

        Double progressPercentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            double pct = currentProgress.multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .doubleValue();
            progressPercentage = Math.round(pct * 100.0) / 100.0;
        }

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount().setScale(2, RoundingMode.HALF_UP),
                goal.getTargetDate(),
                goal.getStartDate(),
                currentProgress,
                progressPercentage,
                remainingAmount
        );
    }
}
