package com.syfe.finance.controller;

import com.syfe.finance.dto.ReportDtos;
import com.syfe.finance.security.CurrentUserService;
import com.syfe.finance.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final CurrentUserService currentUserService;
    private final ReportService reportService;

    public ReportController(CurrentUserService currentUserService, ReportService reportService) {
        this.currentUserService = currentUserService;
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ReportDtos.MonthlyReportResponse monthly(Authentication authentication,
                                                    @PathVariable int year,
                                                    @PathVariable int month) {
        Long userId = currentUserService.requireUserId(authentication);
        return reportService.monthly(userId, year, month);
    }

    @GetMapping("/yearly/{year}")
    public ReportDtos.YearlyReportResponse yearly(Authentication authentication, @PathVariable int year) {
        Long userId = currentUserService.requireUserId(authentication);
        return reportService.yearly(userId, year);
    }
}
