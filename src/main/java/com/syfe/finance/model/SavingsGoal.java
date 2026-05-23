package com.syfe.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoal {
    private Long id;
    private Long userId;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;

    public SavingsGoal(Long id, Long userId, String goalName, BigDecimal targetAmount,
                       LocalDate targetDate, LocalDate startDate) {
        this.id = id;
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.startDate = startDate;
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

    public String getGoalName() {
        return goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
}
