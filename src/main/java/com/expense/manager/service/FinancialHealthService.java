package com.expense.manager.service;

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
public class FinancialHealthService {

    private final ExpenseRepository expenseRepository;

    public Map<String, Object> calculateHealthScore(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate thisMonthEnd = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        BigDecimal income = expenseRepository.getTotalIncomeByDateRange(userId, thisMonthStart, thisMonthEnd);
        BigDecimal expenses = expenseRepository.getTotalExpensesByDateRange(userId, thisMonthStart, thisMonthEnd);
        BigDecimal lastMonthExpenses = expenseRepository.getTotalExpensesByDateRange(userId, lastMonthStart, lastMonthEnd);

        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;
        if (lastMonthExpenses == null) lastMonthExpenses = BigDecimal.ZERO;

        int savingsScore = calculateSavingsScore(income, expenses);
        int budgetScore = calculateBudgetScore(lastMonthExpenses, expenses);
        int trendScore = calculateTrendScore(lastMonthExpenses, expenses);
        int consistencyScore = 15;
        int emergencyScore = calculateEmergencyScore(income, expenses);

        int totalScore = Math.min(100, savingsScore + budgetScore + trendScore + consistencyScore + emergencyScore);

        String status;
        if (totalScore >= 80) status = "EXCELLENT";
        else if (totalScore >= 60) status = "GOOD";
        else if (totalScore >= 40) status = "FAIR";
        else status = "NEEDS ATTENTION";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", totalScore);
        result.put("status", status);
        result.put("factors", List.of(
                Map.of("name", "Savings Rate", "score", savingsScore, "max", 25, "description", "How much of your income you save"),
                Map.of("name", "Budget Discipline", "score", budgetScore, "max", 20, "description", "Staying within your budgets"),
                Map.of("name", "Spending Trend", "score", trendScore, "max", 20, "description", "Month-over-month spending control"),
                Map.of("name", "Consistency", "score", consistencyScore, "max", 15, "description", "Regular expense tracking"),
                Map.of("name", "Emergency Fund", "score", emergencyScore, "max", 20, "description", "Months of expenses covered by savings")
        ));
        result.put("suggestions", generateSuggestions(totalScore, savingsScore, budgetScore, trendScore));
        return result;
    }

    private int calculateSavingsScore(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return 0;
        BigDecimal savingsRate = income.subtract(expenses).multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP);
        double rate = savingsRate.doubleValue();
        if (rate >= 40) return 25;
        if (rate >= 30) return 20;
        if (rate >= 20) return 15;
        if (rate >= 10) return 10;
        return Math.max(0, (int)(rate * 2));
    }

    private int calculateBudgetScore(BigDecimal lastMonth, BigDecimal thisMonth) {
        if (lastMonth.compareTo(BigDecimal.ZERO) == 0) return 15;
        double ratio = thisMonth.divide(lastMonth, 2, RoundingMode.HALF_UP).doubleValue();
        if (ratio <= 0.8) return 20;
        if (ratio <= 0.9) return 17;
        if (ratio <= 1.0) return 15;
        if (ratio <= 1.1) return 10;
        return 5;
    }

    private int calculateTrendScore(BigDecimal lastMonth, BigDecimal thisMonth) {
        if (lastMonth.compareTo(BigDecimal.ZERO) == 0) return 15;
        double change = thisMonth.subtract(lastMonth).multiply(BigDecimal.valueOf(100)).divide(lastMonth, 2, RoundingMode.HALF_UP).doubleValue();
        if (change <= -10) return 20;
        if (change <= 0) return 17;
        if (change <= 10) return 12;
        if (change <= 25) return 8;
        return 3;
    }

    private int calculateEmergencyScore(BigDecimal income, BigDecimal expenses) {
        if (expenses.compareTo(BigDecimal.ZERO) == 0) return 20;
        BigDecimal savings = income.subtract(expenses);
        if (savings.compareTo(BigDecimal.ZERO) <= 0) return 0;
        double months = savings.divide(expenses, 2, RoundingMode.HALF_UP).doubleValue();
        if (months >= 6) return 20;
        if (months >= 3) return 15;
        if (months >= 1) return 10;
        return 5;
    }

    private List<String> generateSuggestions(int total, int savings, int budget, int trend) {
        List<String> suggestions = new ArrayList<>();
        if (savings < 15) suggestions.add("Try to save at least 20% of your income");
        if (budget < 12) suggestions.add("Set category budgets and track them weekly");
        if (trend < 12) suggestions.add("Your spending increased this month — review discretionary expenses");
        if (total >= 80) suggestions.add("Great job! Consider investing your surplus savings");
        if (suggestions.isEmpty()) suggestions.add("Keep tracking your expenses consistently!");
        return suggestions;
    }
}
