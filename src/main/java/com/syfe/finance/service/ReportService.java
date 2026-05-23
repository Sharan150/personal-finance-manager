package com.syfe.finance.service;

import com.syfe.finance.dto.ReportDtos;
import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.FinancialTransaction;
import com.syfe.finance.model.TransactionType;
import com.syfe.finance.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public ReportDtos.MonthlyReportResponse monthly(Long userId, int year, int month) {
        if (month < 1 || month > 12) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Month must be between 1 and 12");
        }
        YearMonth requestedMonth = YearMonth.of(year, month);
        List<FinancialTransaction> transactions = transactionRepository.findActiveByUser(userId).stream()
                .filter(transaction -> YearMonth.from(transaction.getDate()).equals(requestedMonth))
                .toList();

        Totals totals = aggregate(transactions);
        return new ReportDtos.MonthlyReportResponse(
                month,
                year,
                totals.income(),
                totals.expenses(),
                totals.netSavings()
        );
    }

    public ReportDtos.YearlyReportResponse yearly(Long userId, int year) {
        List<FinancialTransaction> transactions = transactionRepository.findActiveByUser(userId).stream()
                .filter(transaction -> transaction.getDate().getYear() == year)
                .toList();

        Totals totals = aggregate(transactions);
        return new ReportDtos.YearlyReportResponse(
                year,
                totals.income(),
                totals.expenses(),
                totals.netSavings()
        );
    }

    private Totals aggregate(List<FinancialTransaction> transactions) {
        Map<String, BigDecimal> income = new LinkedHashMap<>();
        Map<String, BigDecimal> expenses = new LinkedHashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (FinancialTransaction transaction : transactions) {
            Map<String, BigDecimal> targetMap = transaction.getType() == TransactionType.INCOME ? income : expenses;
            targetMap.merge(transaction.getCategory(), transaction.getAmount(), BigDecimal::add);
            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else {
                totalExpenses = totalExpenses.add(transaction.getAmount());
            }
        }

        income.replaceAll((key, value) -> MoneyUtils.money(value));
        expenses.replaceAll((key, value) -> MoneyUtils.money(value));
        return new Totals(income, expenses, MoneyUtils.money(totalIncome.subtract(totalExpenses)));
    }

    private record Totals(
            Map<String, BigDecimal> income,
            Map<String, BigDecimal> expenses,
            BigDecimal netSavings
    ) {
    }
}
