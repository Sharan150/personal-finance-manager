package com.syfe.finance.repository;

import com.syfe.finance.model.Category;
import com.syfe.finance.model.TransactionType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Repository
public class CategoryRepository {
    private final AtomicLong sequence = new AtomicLong(1);
    private final List<Category> defaultCategories = new CopyOnWriteArrayList<>();
    private final Map<Long, Category> customCategoriesById = new ConcurrentHashMap<>();

    @PostConstruct
    void seedDefaults() {
        defaultCategories.add(new Category(sequence.getAndIncrement(), null, "Salary", TransactionType.INCOME, false));
        defaultCategories.add(new Category(sequence.getAndIncrement(), null, "Food", TransactionType.EXPENSE, false));
        defaultCategories.add(new Category(sequence.getAndIncrement(), null, "Rent", TransactionType.EXPENSE, false));
        defaultCategories.add(new Category(sequence.getAndIncrement(), null, "Transportation", TransactionType.EXPENSE, false));
        defaultCategories.add(new Category(sequence.getAndIncrement(), null, "Entertainment", TransactionType.EXPENSE, false));
        defaultCategories.add(new Category(sequence.getAndIncrement(), null, "Healthcare", TransactionType.EXPENSE, false));
        defaultCategories.add(new Category(sequence.getAndIncrement(), null, "Utilities", TransactionType.EXPENSE, false));
    }

    public List<Category> findAllAccessible(Long userId) {
        return accessibleStream(userId)
                .sorted(Comparator.comparing(Category::isCustom).thenComparing(Category::getId))
                .toList();
    }

    public Optional<Category> findByNameAccessible(Long userId, String name) {
        String normalized = normalize(name);
        return accessibleStream(userId)
                .filter(category -> normalize(category.getName()).equals(normalized))
                .findFirst();
    }

    public Optional<Category> findByIdAccessible(Long userId, Long id) {
        return accessibleStream(userId)
                .filter(category -> category.getId().equals(id))
                .findFirst();
    }

    public Category saveCustom(Long userId, String name, TransactionType type) {
        Category category = new Category(sequence.getAndIncrement(), userId, name.trim(), type, true);
        customCategoriesById.put(category.getId(), category);
        return category;
    }

    public void markDeleted(Category category) {
        category.setDeleted(true);
    }

    private Stream<Category> accessibleStream(Long userId) {
        Stream<Category> defaults = defaultCategories.stream();
        Stream<Category> custom = customCategoriesById.values().stream()
                .filter(category -> !category.isDeleted())
                .filter(category -> category.getUserId().equals(userId));
        return Stream.concat(defaults, custom);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
