package com.expense.manager.controller;

import com.expense.manager.ml.AnomalyDetectionService;
import com.expense.manager.ml.SpendingPredictionService;
import com.expense.manager.service.FinancialHealthService;
import com.expense.manager.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private final FinancialHealthService healthService;
    private final AnomalyDetectionService anomalyService;
    private final SpendingPredictionService predictionService;
    private final UserContext userContext;

    @GetMapping("/health-score")
    public ResponseEntity<Map<String, Object>> getHealthScore() {
        return ResponseEntity.ok(healthService.calculateHealthScore(userContext.getCurrentUserId()));
    }

    @GetMapping("/anomalies")
    public ResponseEntity<List<Map<String, Object>>> getAnomalies() {
        return ResponseEntity.ok(anomalyService.detectAnomalies(userContext.getCurrentUserId()));
    }

    @GetMapping("/predictions")
    public ResponseEntity<Map<String, Object>> getPredictions() {
        return ResponseEntity.ok(predictionService.predictMonthlySpending(userContext.getCurrentUserId()));
    }

    @GetMapping("/what-if")
    public ResponseEntity<Map<String, Object>> whatIf(
            @RequestParam String category,
            @RequestParam BigDecimal reduction) {
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("category", category);
        result.put("reduction", reduction);
        result.put("monthlySavings", reduction);
        result.put("annualSavings", reduction.multiply(BigDecimal.valueOf(12)));
        result.put("message", "If you reduce " + category + " spending by ₹" + reduction + ", you'll save ₹" + reduction.multiply(BigDecimal.valueOf(12)) + "/year.");
        return ResponseEntity.ok(result);
    }
}
