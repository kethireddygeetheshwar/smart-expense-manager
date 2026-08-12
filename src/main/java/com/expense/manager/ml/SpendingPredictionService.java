package com.expense.manager.ml;

import com.expense.manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SpendingPredictionService {

    private final ExpenseRepository expenseRepository;

    public Map<String, Object> predictMonthlySpending(Long userId) {
        List<Object[]> monthlyData = expenseRepository.getMonthlySpending(userId, java.time.LocalDate.now().getYear());
        Map<String, Object> prediction = new LinkedHashMap<>();

        if (monthlyData == null || monthlyData.isEmpty()) {
            prediction.put("predictedAmount", BigDecimal.ZERO);
            prediction.put("confidence", 0);
            prediction.put("breakdown", List.of());
            return prediction;
        }

        double[] amounts = monthlyData.stream().mapToDouble(r -> ((BigDecimal) r[1]).doubleValue()).toArray();
        double predictedAmount = simpleMovingAverage(amounts, 3);
        double trend = calculateTrend(amounts);
        predictedAmount = Math.max(0, predictedAmount + trend);

        BigDecimal totalPredicted = BigDecimal.valueOf(predictedAmount).setScale(0, RoundingMode.HALF_UP);
        int confidence = Math.min(95, Math.max(50, 100 - (int)(standardDeviation(amounts) / predictedAmount * 100)));

        List<Map<String, Object>> breakdown = new ArrayList<>();
        LocalDate now = java.time.LocalDate.now();
        List<Object[]> categoryData = expenseRepository.getCategorySpending(userId, now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()));

        if (categoryData != null) {
            for (Object[] row : categoryData) {
                String category = ((Enum<?>) row[0]).name();
                BigDecimal amount = (BigDecimal) row[1];
                double projected = amount.doubleValue() * (predictedAmount > 0 ? predictedAmount / Arrays.stream(amounts).sum() : 1);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("category", category);
                item.put("currentAmount", amount);
                item.put("predictedAmount", BigDecimal.valueOf(projected).setScale(0, RoundingMode.HALF_UP));
                item.put("willExceedBudget", false);
                breakdown.add(item);
            }
        }

        prediction.put("predictedAmount", totalPredicted);
        prediction.put("confidence", confidence);
        prediction.put("trend", trend > 0 ? "increasing" : trend < 0 ? "decreasing" : "stable");
        prediction.put("breakdown", breakdown);
        prediction.put("message", "At your current spending rate, you're projected to spend " + formatCurrency(totalPredicted) + " this month.");

        return prediction;
    }

    private double simpleMovingAverage(double[] data, int window) {
        if (data.length == 0) return 0;
        int n = Math.min(window, data.length);
        double sum = 0;
        for (int i = data.length - n; i < data.length; i++) sum += data[i];
        return sum / n;
    }

    private double calculateTrend(double[] data) {
        if (data.length < 2) return 0;
        return (data[data.length - 1] - data[0]) / data.length;
    }

    private double standardDeviation(double[] data) {
        if (data.length == 0) return 0;
        double mean = Arrays.stream(data).average().orElse(0);
        double variance = Arrays.stream(data).map(d -> Math.pow(d - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

    private String formatCurrency(BigDecimal amount) {
        return "₹" + String.format("%,.0f", amount.doubleValue());
    }
}
