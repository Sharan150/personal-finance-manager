package com.syfe.finance.service;

import com.syfe.finance.dto.TransactionDtos;
import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.Category;
import com.syfe.finance.model.FinancialTransaction;
import com.syfe.finance.model.TransactionType;
import com.syfe.finance.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
    }

    public TransactionDtos.TransactionResponse create(Long userId, TransactionDtos.CreateTransactionRequest request) {
        validateNotFuture(request.date());
        Category category = categoryService.resolveByName(userId, request.category());
        FinancialTransaction transaction = new FinancialTransaction(
                null,
                userId,
                MoneyUtils.money(request.amount()),
                request.date(),
                category.getName(),
                category.getType(),
                cleanOptional(request.description())
        );
        return toResponse(transactionRepository.save(transaction));
    }

    public TransactionDtos.TransactionsResponse getTransactions(Long userId, LocalDate startDate, LocalDate endDate,
                                                                Long categoryId, String categoryName,
                                                                TransactionType type) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startDate cannot be after endDate");
        }

        List<FinancialTransaction> filtered = transactionRepository.findActiveByUser(userId).stream()
                .filter(transaction -> startDate == null || !transaction.getDate().isBefore(startDate))
                .filter(transaction -> endDate == null || !transaction.getDate().isAfter(endDate))
                .filter(transaction -> type == null || transaction.getType() == type)
                .filter(transaction -> matchesCategory(userId, transaction, categoryId, categoryName))
                .toList();

        return new TransactionDtos.TransactionsResponse(filtered.stream().map(this::toResponse).toList());
    }

    public TransactionDtos.TransactionResponse update(Long userId, Long id, TransactionDtos.UpdateTransactionRequest request) {
        FinancialTransaction transaction = findOwned(userId, id);

        if (request.amount() != null) {
            transaction.setAmount(MoneyUtils.money(request.amount()));
        }
        if (request.category() != null) {
            Category category = categoryService.resolveByName(userId, request.category());
            transaction.setCategory(category.getName());
            transaction.setType(category.getType());
        }
        if (request.description() != null) {
            transaction.setDescription(cleanOptional(request.description()));
        }
        return toResponse(transactionRepository.save(transaction));
    }

    public void delete(Long userId, Long id) {
        FinancialTransaction transaction = findOwned(userId, id);
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
    }

    public FinancialTransaction findOwned(Long userId, Long id) {
        return transactionRepository.findActiveByUserAndId(userId, id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    private boolean matchesCategory(Long userId, FinancialTransaction transaction, Long categoryId, String categoryName) {
        if (categoryId != null) {
            Category category = categoryService.resolveById(userId, categoryId);
            return category.getName().equalsIgnoreCase(transaction.getCategory());
        }
        if (categoryName != null && !categoryName.isBlank()) {
            return categoryName.trim().equalsIgnoreCase(transaction.getCategory());
        }
        return true;
    }

    private void validateNotFuture(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Transaction date cannot be in the future");
        }
    }

    private String cleanOptional(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }

    private TransactionDtos.TransactionResponse toResponse(FinancialTransaction transaction) {
        BigDecimal amount = MoneyUtils.money(transaction.getAmount());
        return new TransactionDtos.TransactionResponse(
                transaction.getId(),
                amount,
                transaction.getDate(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getType()
        );
    }
}
