package com.finance.service;

import com.finance.dto.MonthlyReportResponse;
import com.finance.dto.YearlyReportResponse;
import com.finance.entity.CategoryType;
import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse generateMonthlyReport(Integer year, Integer month, User user) {
        if (month == null || month < 1 || month > 12) {
            throw new BadRequestException("Invalid month: " + month + ". Month must be between 1 and 12.");
        }
        if (year == null || year < 1900) {
            throw new BadRequestException("Invalid year: " + year);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome.merge(catName, amount, BigDecimal::add);
                sumIncome = sumIncome.add(amount);
            } else if (t.getCategory().getType() == CategoryType.EXPENSE) {
                totalExpenses.merge(catName, amount, BigDecimal::add);
                sumExpenses = sumExpenses.add(amount);
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpenses).setScale(2, RoundingMode.HALF_UP);

        return new MonthlyReportResponse(month, year, totalIncome, totalExpenses, netSavings);
    }

    @Transactional(readOnly = true)
    public YearlyReportResponse generateYearlyReport(Integer year, User user) {
        if (year == null || year < 1900) {
            throw new BadRequestException("Invalid year: " + year);
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome.merge(catName, amount, BigDecimal::add);
                sumIncome = sumIncome.add(amount);
            } else if (t.getCategory().getType() == CategoryType.EXPENSE) {
                totalExpenses.merge(catName, amount, BigDecimal::add);
                sumExpenses = sumExpenses.add(amount);
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpenses).setScale(2, RoundingMode.HALF_UP);

        return new YearlyReportResponse(year, totalIncome, totalExpenses, netSavings);
    }
}
