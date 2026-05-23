package com.syfe.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class GoalDtos {
    private GoalDtos() {
    }

    public record CreateGoalRequest(
            @NotBlank String goalName,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal targetAmount,
            @NotNull LocalDate targetDate,
            LocalDate startDate
    ) {
    }

    public record UpdateGoalRequest(
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal targetAmount,
            LocalDate targetDate
    ) {
    }

    public record GoalResponse(
            Long id,
            String goalName,
            BigDecimal targetAmount,
            LocalDate targetDate,
            LocalDate startDate,
            BigDecimal currentProgress,
            BigDecimal progressPercentage,
            BigDecimal remainingAmount
    ) {
    }

    public record GoalsResponse(List<GoalResponse> goals) {
    }
}
