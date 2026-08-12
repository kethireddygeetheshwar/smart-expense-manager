package com.expense.manager.controller;

import com.expense.manager.service.AnalyticsService;
import com.expense.manager.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserContext userContext;

    @GetMapping("/monthly-comparison")
    public ResponseEntity<Map<String, Object>> getMonthlyComparison() {
        return ResponseEntity.ok(analyticsService.getMonthlyComparison(userContext.getCurrentUserId()));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<Map<String, Object>> getCategoryBreakdown(
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(userContext.getCurrentUserId(), months));
    }

    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getSpendingInsights() {
        return ResponseEntity.ok(analyticsService.getSpendingInsights(userContext.getCurrentUserId()));
    }
}
