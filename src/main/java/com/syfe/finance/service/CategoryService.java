package com.syfe.finance.service;

import com.syfe.finance.dto.CategoryDtos;
import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.Category;
import com.syfe.finance.repository.CategoryRepository;
import com.syfe.finance.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public CategoryDtos.CategoriesResponse getCategories(Long userId) {
        List<CategoryDtos.CategoryResponse> categories = categoryRepository.findAllAccessible(userId).stream()
                .map(this::toResponse)
                .toList();
        return new CategoryDtos.CategoriesResponse(categories);
    }

    public CategoryDtos.CategoryResponse createCustomCategory(Long userId, CategoryDtos.CreateCategoryRequest request) {
        String name = normalizeVisible(request.name());
        categoryRepository.findByNameAccessible(userId, name).ifPresent(category -> {
            throw new ApiException(HttpStatus.CONFLICT, "Category name already exists");
        });

        Category category = categoryRepository.saveCustom(userId, name, request.type());
        return toResponse(category);
    }

    public void deleteCustomCategory(Long userId, String name) {
        Category category = resolveByName(userId, name);
        if (!category.isCustom()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Default categories cannot be deleted");
        }
        if (transactionRepository.existsActiveByUserAndCategory(userId, category.getName())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category is referenced by transactions");
        }
        categoryRepository.markDeleted(category);
    }

    public Category resolveByName(Long userId, String name) {
        String normalizedName = normalizeVisible(name);
        return categoryRepository.findByNameAccessible(userId, normalizedName)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Category does not exist"));
    }

    public Category resolveById(Long userId, Long categoryId) {
        return categoryRepository.findByIdAccessible(userId, categoryId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Category does not exist"));
    }

    private CategoryDtos.CategoryResponse toResponse(Category category) {
        return new CategoryDtos.CategoryResponse(category.getName(), category.getType(), category.isCustom());
    }

    private String normalizeVisible(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
        return value.trim();
    }
}
