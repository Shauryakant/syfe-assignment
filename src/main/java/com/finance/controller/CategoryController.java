package com.finance.controller;

import com.finance.dto.CategoryDto;
import com.finance.dto.CategoryListResponse;
import com.finance.dto.MessageResponse;
import com.finance.entity.User;
import com.finance.service.CategoryService;
import com.finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired(required = false)
    private CategoryService.CategoryUsageChecker categoryUsageChecker;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        CategoryListResponse response = categoryService.getAllCategoriesForUser(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCustomCategory(@Valid @RequestBody CategoryDto dto,
                                                             Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        CategoryDto created = categoryService.createCustomCategory(dto, user);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCustomCategory(@PathVariable String name,
                                                               Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        categoryService.deleteCustomCategory(name, user, categoryUsageChecker);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }
}
