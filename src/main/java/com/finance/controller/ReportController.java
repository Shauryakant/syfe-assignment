package com.finance.controller;

import com.finance.dto.MonthlyReportResponse;
import com.finance.dto.YearlyReportResponse;
import com.finance.entity.User;
import com.finance.service.ReportService;
import com.finance.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@PathVariable Integer year,
                                                                   @PathVariable Integer month,
                                                                   Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        MonthlyReportResponse response = reportService.generateMonthlyReport(year, month, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable Integer year,
                                                                 Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        YearlyReportResponse response = reportService.generateYearlyReport(year, user);
        return ResponseEntity.ok(response);
    }
}
