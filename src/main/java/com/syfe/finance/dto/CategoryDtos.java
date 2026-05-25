package com.syfe.finance.dto;

import com.syfe.finance.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class CategoryDtos {
    private CategoryDtos() {
    }

    public record CreateCategoryRequest(
            @NotBlank String name,
            @NotNull TransactionType type
    ) {
    }

    public record CategoryResponse(String name, TransactionType type, @com.fasterxml.jackson.annotation.JsonProperty("custom") boolean isCustom) {
    }

    public record CategoriesResponse(List<CategoryResponse> categories) {
    }
}
