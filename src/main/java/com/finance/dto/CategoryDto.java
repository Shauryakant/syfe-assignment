package com.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryDto {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    @JsonProperty("isCustom")
    private Boolean isCustom;

    public CategoryDto() {}

    public CategoryDto(String name, CategoryType type, Boolean isCustom) {
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    @JsonProperty("isCustom")
    public Boolean getIsCustom() { return isCustom; }

    @JsonProperty("custom")
    public Boolean getCustom() { return isCustom; }

    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }
}
