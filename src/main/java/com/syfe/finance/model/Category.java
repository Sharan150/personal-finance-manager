package com.syfe.finance.model;

public class Category {
    private Long id;
    private Long userId;
    private String name;
    private TransactionType type;
    private boolean custom;
    private boolean deleted;

    public Category(Long id, Long userId, String name, TransactionType type, boolean custom) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.custom = custom;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public TransactionType getType() {
        return type;
    }

    public boolean isCustom() {
        return custom;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
