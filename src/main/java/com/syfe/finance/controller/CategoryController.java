package com.syfe.finance.controller;

import com.syfe.finance.dto.AuthDtos;
import com.syfe.finance.dto.CategoryDtos;
import com.syfe.finance.security.CurrentUserService;
import com.syfe.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CurrentUserService currentUserService;
    private final CategoryService categoryService;

    public CategoryController(CurrentUserService currentUserService, CategoryService categoryService) {
        this.currentUserService = currentUserService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public CategoryDtos.CategoriesResponse getCategories(Authentication authentication) {
        Long userId = currentUserService.requireUserId(authentication);
        return categoryService.getCategories(userId);
    }

    @PostMapping
    public ResponseEntity<CategoryDtos.CategoryResponse> create(Authentication authentication,
                                                               @Valid @RequestBody CategoryDtos.CreateCategoryRequest request) {
        Long userId = currentUserService.requireUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCustomCategory(userId, request));
    }

    @DeleteMapping("/{name}")
    public AuthDtos.MessageResponse delete(Authentication authentication, @PathVariable String name) {
        Long userId = currentUserService.requireUserId(authentication);
        categoryService.deleteCustomCategory(userId, name);
        return new AuthDtos.MessageResponse("Category deleted successfully");
    }
}
