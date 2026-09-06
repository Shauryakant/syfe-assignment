package com.finance.controller;

import com.finance.dto.MessageResponse;
import com.finance.dto.TransactionListResponse;
import com.finance.dto.TransactionRequest;
import com.finance.dto.TransactionResponse;
import com.finance.entity.User;
import com.finance.service.TransactionService;
import com.finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request,
                                                                 Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        TransactionResponse response = transactionService.createTransaction(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        TransactionListResponse response = transactionService.getTransactions(user, startDate, endDate, category);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequest request,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        TransactionResponse response = transactionService.updateTransaction(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.ok(new MessageResponse("Transaction deleted successfully"));
    }
}
