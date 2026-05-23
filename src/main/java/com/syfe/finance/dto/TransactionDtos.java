package com.syfe.finance.dto;

import com.syfe.finance.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class TransactionDtos {
    private TransactionDtos() {
    }

    public record CreateTransactionRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotNull LocalDate date,
            @NotBlank String category,
            String description
    ) {
    }

    public record UpdateTransactionRequest(
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            String category,
            String description,
            LocalDate date
    ) {
    }

    public record TransactionResponse(
            Long id,
            BigDecimal amount,
            LocalDate date,
            String category,
            String description,
            TransactionType type
    ) {
    }

    public record TransactionsResponse(List<TransactionResponse> transactions) {
    }
}
