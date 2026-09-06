package com.finance.controller;

import com.finance.dto.MessageResponse;
import com.finance.dto.SavingsGoalListResponse;
import com.finance.dto.SavingsGoalRequest;
import com.finance.dto.SavingsGoalResponse;
import com.finance.entity.User;
import com.finance.service.SavingsGoalService;
import com.finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final UserService userService;

    public SavingsGoalController(SavingsGoalService savingsGoalService, UserService userService) {
        this.savingsGoalService = savingsGoalService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody SavingsGoalRequest request,
                                                           Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        SavingsGoalResponse response = savingsGoalService.createGoal(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<SavingsGoalListResponse> getAllGoals(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        SavingsGoalListResponse response = savingsGoalService.getAllGoals(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoalById(@PathVariable Long id,
                                                            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        SavingsGoalResponse response = savingsGoalService.getGoalById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(@PathVariable Long id,
                                                           @RequestBody SavingsGoalRequest request,
                                                           Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        SavingsGoalResponse response = savingsGoalService.updateGoal(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id,
                                                      Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        savingsGoalService.deleteGoal(id, user);
        return ResponseEntity.ok(new MessageResponse("Goal deleted successfully"));
    }
}
