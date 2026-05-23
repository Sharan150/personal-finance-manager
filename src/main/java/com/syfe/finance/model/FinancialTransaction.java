package com.syfe.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialTransaction {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private TransactionType type;
    private String description;
    private boolean deleted;

    public FinancialTransaction(Long id, Long userId, BigDecimal amount, LocalDate date,
                                String category, TransactionType type, String description) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.type = type;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
