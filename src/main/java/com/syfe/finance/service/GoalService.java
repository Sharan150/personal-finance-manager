package com.syfe.finance.service;

import com.syfe.finance.dto.GoalDtos;
import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.FinancialTransaction;
import com.syfe.finance.model.SavingsGoal;
import com.syfe.finance.model.TransactionType;
import com.syfe.finance.repository.SavingsGoalRepository;
import com.syfe.finance.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {
    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    public GoalService(SavingsGoalRepository goalRepository, TransactionRepository transactionRepository) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
    }

    public GoalDtos.GoalResponse create(Long userId, GoalDtos.CreateGoalRequest request) {
        validateFutureTargetDate(request.targetDate());
        LocalDate startDate = request.startDate() == null ? LocalDate.now() : request.startDate();
        SavingsGoal goal = new SavingsGoal(
                null,
                userId,
                request.goalName().trim(),
                MoneyUtils.money(request.targetAmount()),
                request.targetDate(),
                startDate
        );
        return toResponse(goalRepository.save(goal));
    }

    public GoalDtos.GoalsResponse getAll(Long userId) {
        return new GoalDtos.GoalsResponse(goalRepository.findByUser(userId).stream()
                .map(this::toResponse)
                .toList());
    }

    public GoalDtos.GoalResponse get(Long userId, Long id) {
        return toResponse(findOwnedOrForbidden(userId, id));
    }

    public GoalDtos.GoalResponse update(Long userId, Long id, GoalDtos.UpdateGoalRequest request) {
        SavingsGoal goal = findOwnedOrForbidden(userId, id);
        if (request.targetAmount() != null) {
            goal.setTargetAmount(MoneyUtils.money(request.targetAmount()));
        }
        if (request.targetDate() != null) {
            validateFutureTargetDate(request.targetDate());
            goal.setTargetDate(request.targetDate());
        }
        return toResponse(goalRepository.save(goal));
    }

    public void delete(Long userId, Long id) {
        findOwnedOrForbidden(userId, id);
        goalRepository.delete(id);
    }

    private SavingsGoal findOwnedOrForbidden(Long userId, Long id) {
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Goal not found"));
        if (!goal.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Goal belongs to another user");
        }
        return goal;
    }

    private GoalDtos.GoalResponse toResponse(SavingsGoal goal) {
        BigDecimal progress = calculateProgress(goal.getUserId(), goal.getStartDate());
        BigDecimal percentage = progress.multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        BigDecimal remaining = goal.getTargetAmount().subtract(progress).max(BigDecimal.ZERO);

        return new GoalDtos.GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                MoneyUtils.money(goal.getTargetAmount()),
                goal.getTargetDate(),
                goal.getStartDate(),
                MoneyUtils.moneyOrZero(progress),
                MoneyUtils.formatPercentage(percentage),
                MoneyUtils.moneyOrZero(remaining)
        );
    }

    private BigDecimal calculateProgress(Long userId, LocalDate startDate) {
        List<FinancialTransaction> transactions = transactionRepository.findActiveByUserSince(userId, startDate);
        BigDecimal progress = BigDecimal.ZERO;
        for (FinancialTransaction transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                progress = progress.add(transaction.getAmount());
            } else {
                progress = progress.subtract(transaction.getAmount());
            }
        }
        return progress;
    }

    private void validateFutureTargetDate(LocalDate targetDate) {
        if (!targetDate.isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Target date must be in the future");
        }
    }
}
