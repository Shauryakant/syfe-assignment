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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "John", "123");
        testUser.setId(1L);
    }

    @Test
    void testInitDefaultCategories() {
        when(categoryRepository.existsByNameAndIsCustomFalse(anyString())).thenReturn(false);
        categoryService.initDefaultCategories();
        verify(categoryRepository, times(7)).save(any(Category.class));
    }

    @Test
    void testGetAllCategoriesForUser() {
        Category c1 = new Category("Salary", CategoryType.INCOME, false, null);
        Category c2 = new Category("CustomInc", CategoryType.INCOME, true, testUser);

        when(categoryRepository.findAllForUser(testUser)).thenReturn(List.of(c1, c2));

        CategoryListResponse response = categoryService.getAllCategoriesForUser(testUser);

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
    }

    @Test
    void testCreateCustomCategory_Success() {
        CategoryDto dto = new CategoryDto("Freelance", CategoryType.INCOME, null);
        when(categoryRepository.existsByNameAndIsCustomFalse("Freelance")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("Freelance", testUser)).thenReturn(false);

        Category saved = new Category("Freelance", CategoryType.INCOME, true, testUser);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.createCustomCategory(dto, testUser);

        assertNotNull(result);
        assertEquals("Freelance", result.getName());
        assertTrue(result.getIsCustom());
    }

    @Test
    void testCreateCustomCategory_Duplicate() {
        CategoryDto dto = new CategoryDto("Salary", CategoryType.INCOME, null);
        when(categoryRepository.existsByNameAndIsCustomFalse("Salary")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCustomCategory(dto, testUser));
    }

    @Test
    void testCreateCustomCategory_InvalidInputs() {
        assertThrows(BadRequestException.class, () -> categoryService.createCustomCategory(new CategoryDto("", CategoryType.INCOME, null), testUser));
        assertThrows(BadRequestException.class, () -> categoryService.createCustomCategory(new CategoryDto("Name", null, null), testUser));
    }

    @Test
    void testDeleteCustomCategory_Success() {
        Category category = new Category("Freelance", CategoryType.INCOME, true, testUser);
        when(categoryRepository.existsByNameAndIsCustomFalse("Freelance")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Freelance", testUser)).thenReturn(Optional.of(category));

        categoryService.deleteCustomCategory("Freelance", testUser, (cat, u) -> false);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void testDeleteCustomCategory_DefaultCategory() {
        when(categoryRepository.existsByNameAndIsCustomFalse("Salary")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCustomCategory("Salary", testUser, null));
    }

    @Test
    void testDeleteCustomCategory_InUse() {
        Category category = new Category("Freelance", CategoryType.INCOME, true, testUser);
        when(categoryRepository.existsByNameAndIsCustomFalse("Freelance")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Freelance", testUser)).thenReturn(Optional.of(category));

        assertThrows(BadRequestException.class, () -> categoryService.deleteCustomCategory("Freelance", testUser, (cat, u) -> true));
    }

    @Test
    void testDeleteCustomCategory_NotFound() {
        when(categoryRepository.existsByNameAndIsCustomFalse("NonExistent")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("NonExistent", testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCustomCategory("NonExistent", testUser, null));
    }
}
