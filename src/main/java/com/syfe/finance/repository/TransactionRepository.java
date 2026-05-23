package com.syfe.finance.repository;

import com.syfe.finance.model.FinancialTransaction;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TransactionRepository {
    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, FinancialTransaction> transactionsById = new ConcurrentHashMap<>();

    public synchronized FinancialTransaction save(FinancialTransaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(sequence.getAndIncrement());
        }
        transactionsById.put(transaction.getId(), transaction);
        return transaction;
    }

    public Optional<FinancialTransaction> findActiveByUserAndId(Long userId, Long id) {
        return Optional.ofNullable(transactionsById.get(id))
                .filter(transaction -> !transaction.isDeleted())
                .filter(transaction -> transaction.getUserId().equals(userId));
    }

    public List<FinancialTransaction> findActiveByUser(Long userId) {
        return transactionsById.values().stream()
                .filter(transaction -> !transaction.isDeleted())
                .filter(transaction -> transaction.getUserId().equals(userId))
                .sorted(Comparator.comparing(FinancialTransaction::getDate).reversed()
                        .thenComparing(Comparator.comparing(FinancialTransaction::getId).reversed()))
                .toList();
    }

    public boolean existsActiveByUserAndCategory(Long userId, String categoryName) {
        String normalized = normalize(categoryName);
        return transactionsById.values().stream()
                .filter(transaction -> !transaction.isDeleted())
                .filter(transaction -> transaction.getUserId().equals(userId))
                .anyMatch(transaction -> normalize(transaction.getCategory()).equals(normalized));
    }

    public List<FinancialTransaction> findActiveByUserSince(Long userId, LocalDate startDate) {
        return findActiveByUser(userId).stream()
                .filter(transaction -> !transaction.getDate().isBefore(startDate))
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
