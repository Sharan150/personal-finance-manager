package com.syfe.finance.controller;

import com.syfe.finance.dto.AuthDtos;
import com.syfe.finance.dto.TransactionDtos;
import com.syfe.finance.model.TransactionType;
import com.syfe.finance.security.CurrentUserService;
import com.syfe.finance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final CurrentUserService currentUserService;
    private final TransactionService transactionService;

    public TransactionController(CurrentUserService currentUserService, TransactionService transactionService) {
        this.currentUserService = currentUserService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDtos.TransactionResponse> create(Authentication authentication,
                                                                     @Valid @RequestBody TransactionDtos.CreateTransactionRequest request) {
        Long userId = currentUserService.requireUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(userId, request));
    }

    @GetMapping
    public TransactionDtos.TransactionsResponse getTransactions(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type) {
        Long userId = currentUserService.requireUserId(authentication);
        return transactionService.getTransactions(userId, startDate, endDate, categoryId, category, type);
    }

    @PutMapping("/{id}")
    public TransactionDtos.TransactionResponse update(Authentication authentication,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody TransactionDtos.UpdateTransactionRequest request) {
        Long userId = currentUserService.requireUserId(authentication);
        return transactionService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public AuthDtos.MessageResponse delete(Authentication authentication, @PathVariable Long id) {
        Long userId = currentUserService.requireUserId(authentication);
        transactionService.delete(userId, id);
        return new AuthDtos.MessageResponse("Transaction deleted successfully");
    }
}
