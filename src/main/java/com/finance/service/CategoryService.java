package com.finance.service;

import com.finance.dto.CategoryDto;
import com.finance.dto.CategoryListResponse;
import com.finance.entity.Category;
import com.finance.entity.CategoryType;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.DuplicateResourceException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.CategoryRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDefaultCategories() {
        seedDefaultCategory("Salary", CategoryType.INCOME);
        seedDefaultCategory("Food", CategoryType.EXPENSE);
        seedDefaultCategory("Rent", CategoryType.EXPENSE);
        seedDefaultCategory("Transportation", CategoryType.EXPENSE);
        seedDefaultCategory("Entertainment", CategoryType.EXPENSE);
        seedDefaultCategory("Healthcare", CategoryType.EXPENSE);
        seedDefaultCategory("Utilities", CategoryType.EXPENSE);
    }

    private void seedDefaultCategory(String name, CategoryType type) {
        if (!categoryRepository.existsByNameAndIsCustomFalse(name)) {
            categoryRepository.save(new Category(name, type, false, null));
        }
    }

    @Transactional(readOnly = true)
    public CategoryListResponse getAllCategoriesForUser(User user) {
        List<Category> categories = categoryRepository.findAllForUser(user);
        List<CategoryDto> dtos = categories.stream()
                .map(c -> new CategoryDto(c.getName(), c.getType(), c.isCustom()))
                .toList();
        return new CategoryListResponse(dtos);
    }

    @Transactional
    public CategoryDto createCustomCategory(CategoryDto dto, User user) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }
        if (dto.getType() == null) {
            throw new BadRequestException("Category type is required");
        }

        if (categoryRepository.existsByNameAndIsCustomFalse(dto.getName()) ||
            categoryRepository.existsByNameAndUser(dto.getName(), user)) {
            throw new DuplicateResourceException("Category already exists: " + dto.getName());
        }

        Category category = new Category(dto.getName(), dto.getType(), true, user);
        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved.getName(), saved.getType(), saved.isCustom());
    }

    @Transactional
    public void deleteCustomCategory(String name, User user, CategoryUsageChecker usageChecker) {
        if (categoryRepository.existsByNameAndIsCustomFalse(name)) {
            throw new BadRequestException("Default categories cannot be deleted: " + name);
        }

        Category category = categoryRepository.findByNameAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));

        if (usageChecker != null && usageChecker.isCategoryInUse(category, user)) {
            throw new BadRequestException("Cannot delete category currently referenced by transactions: " + name);
        }

        categoryRepository.delete(category);
    }

    public Category findCategoryByNameForUser(String name, User user) {
        return categoryRepository.findByNameForUser(name, user)
                .orElseThrow(() -> new BadRequestException("Invalid category: " + name));
    }

    @FunctionalInterface
    public interface CategoryUsageChecker {
        boolean isCategoryInUse(Category category, User user);
    }
}
