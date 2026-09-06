package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.CategoryDto;
import com.finance.dto.CategoryListResponse;
import com.finance.entity.CategoryType;
import com.finance.entity.User;
import com.finance.service.CategoryService;
import com.finance.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "pass", "John", "123");
        testUser.setId(1L);
        when(userService.getUserByUsername("user@example.com")).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testGetAllCategories_Success() throws Exception {
        CategoryDto c1 = new CategoryDto("Salary", CategoryType.INCOME, false);
        CategoryDto c2 = new CategoryDto("Freelance", CategoryType.INCOME, true);
        CategoryListResponse response = new CategoryListResponse(List.of(c1, c2));

        when(categoryService.getAllCategoriesForUser(testUser)).thenReturn(response);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Salary"))
                .andExpect(jsonPath("$.categories[1].name").value("Freelance"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testCreateCustomCategory_Success() throws Exception {
        CategoryDto request = new CategoryDto("Freelance", CategoryType.INCOME, null);
        CategoryDto response = new CategoryDto("Freelance", CategoryType.INCOME, true);

        when(categoryService.createCustomCategory(any(CategoryDto.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void testDeleteCustomCategory_Success() throws Exception {
        doNothing().when(categoryService).deleteCustomCategory(eq("Freelance"), eq(testUser), any());

        mockMvc.perform(delete("/api/categories/Freelance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    void testGetCategories_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
}
