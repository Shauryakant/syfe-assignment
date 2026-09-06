package com.finance.dto;

import java.util.List;

public class CategoryListResponse {
    private List<CategoryDto> categories;

    public CategoryListResponse() {}

    public CategoryListResponse(List<CategoryDto> categories) {
        this.categories = categories;
    }

    public List<CategoryDto> getCategories() { return categories; }
    public void setCategories(List<CategoryDto> categories) { this.categories = categories; }
}
