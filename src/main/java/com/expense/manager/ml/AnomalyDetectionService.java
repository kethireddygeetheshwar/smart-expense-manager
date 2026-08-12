package com.expense.manager.ml;

import com.expense.manager.entity.Expense;
import com.expense.manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final ExpenseRepository expenseRepository;

    public List<Map<String, Object>> detectAnomalies(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate threeMonthsAgo = now.minusMonths(3).withDayOfMonth(1);

        List<Object[]> categorySpending = expenseRepository.getCategorySpending(userId, thisMonthStart, now);
        List<Object[]> historicalSpending = expenseRepository.getMonthlyAvgByCategory(userId, threeMonthsAgo, now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth()));

        List<Map<String, Object>> anomalies = new ArrayList<>();

        if (categorySpending == null) return anomalies;

        for (Object[] row : categorySpending) {
            Expense.Category category = (Expense.Category) row[0];
            BigDecimal currentAmount = (BigDecimal) row[1];
            if (currentAmount == null || currentAmount.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal avgAmount = getHistoricalAverage(historicalSpending, category);
            if (avgAmount == null || avgAmount.compareTo(BigDecimal.ZERO) == 0) continue;

            double zScore = calculateZScore(currentAmount, avgAmount, getStdDev(historicalSpending, category, avgAmount));
            double increasePercent = currentAmount.subtract(avgAmount).multiply(BigDecimal.valueOf(100)).divide(avgAmount, 1, RoundingMode.HALF_UP).doubleValue();

            if (zScore > 1.5 || increasePercent > 30) {
                Map<String, Object> anomaly = new LinkedHashMap<>();
                anomaly.put("category", category.name());
                anomaly.put("currentAmount", currentAmount);
                anomaly.put("averageAmount", avgAmount);
                anomaly.put("increasePercent", increasePercent);
                anomaly.put("zScore", Math.round(zScore * 100.0) / 100.0);
                anomaly.put("severity", zScore > 2.5 ? "HIGH" : zScore > 1.8 ? "MEDIUM" : "LOW");
                anomaly.put("anomalyProbability", Math.min(99, (int)(zScore / 3.0 * 100)));
                anomaly.put("message", generateMessage(category.name(), increasePercent, zScore));
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    private BigDecimal getHistoricalAverage(List<Object[]> historical, Expense.Category category) {
        if (historical == null) return null;
        for (Object[] row : historical) {
            if (row[0] == category) return (BigDecimal) row[1];
        }
        return null;
    }

    private BigDecimal getStdDev(List<Object[]> historical, Expense.Category category, BigDecimal avg) {
        if (historical == null) return avg.multiply(BigDecimal.valueOf(0.2));
        for (Object[] row : historical) {
            if (row[0] == category && row[2] != null) return (BigDecimal) row[2];
        }
        return avg.multiply(BigDecimal.valueOf(0.3));
    }

    private double calculateZScore(BigDecimal value, BigDecimal mean, BigDecimal stdDev) {
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) return 0;
        return value.subtract(mean).divide(stdDev, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private String generateMessage(String category, double increase, double zScore) {
        if (increase > 80) return "Your " + category + " spending is " + (int)increase + "% higher than usual — significant anomaly detected.";
        if (increase > 50) return category + " spending is " + (int)increase + "% above your normal pattern.";
        return category + " spending is slightly higher than average.";
    }
}
