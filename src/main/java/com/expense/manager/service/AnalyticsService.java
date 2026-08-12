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
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;

    public Map<String, Object> getMonthlyComparison(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate thisMonthEnd = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        BigDecimal thisMonthExpenses = expenseRepository.getTotalExpensesByDateRange(userId, thisMonthStart, thisMonthEnd);
        BigDecimal lastMonthExpenses = expenseRepository.getTotalExpensesByDateRange(userId, lastMonthStart, lastMonthEnd);
        BigDecimal thisMonthIncome = expenseRepository.getTotalIncomeByDateRange(userId, thisMonthStart, thisMonthEnd);
        BigDecimal lastMonthIncome = expenseRepository.getTotalIncomeByDateRange(userId, lastMonthStart, lastMonthEnd);

        if (thisMonthExpenses == null) thisMonthExpenses = BigDecimal.ZERO;
        if (lastMonthExpenses == null) lastMonthExpenses = BigDecimal.ZERO;
        if (thisMonthIncome == null) thisMonthIncome = BigDecimal.ZERO;
        if (lastMonthIncome == null) lastMonthIncome = BigDecimal.ZERO;

        double expenseChangePercent = lastMonthExpenses.compareTo(BigDecimal.ZERO) > 0
                ? thisMonthExpenses.subtract(lastMonthExpenses).multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthExpenses, 2, RoundingMode.HALF_UP).doubleValue()
                : 0;

        double incomeChangePercent = lastMonthIncome.compareTo(BigDecimal.ZERO) > 0
                ? thisMonthIncome.subtract(lastMonthIncome).multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthIncome, 2, RoundingMode.HALF_UP).doubleValue()
                : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("thisMonthExpenses", thisMonthExpenses);
        result.put("lastMonthExpenses", lastMonthExpenses);
        result.put("expenseChangePercent", expenseChangePercent);
        result.put("thisMonthIncome", thisMonthIncome);
        result.put("lastMonthIncome", lastMonthIncome);
        result.put("incomeChangePercent", incomeChangePercent);
        result.put("trend", expenseChangePercent > 0 ? "increased" : "decreased");

        return result;
    }

    public Map<String, Object> getCategoryBreakdown(Long userId, int months) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months).withDayOfMonth(1);

        List<Object[]> results = expenseRepository.getCategorySpending(userId, start, end);
        Map<String, Object> breakdown = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Object[] row : results) {
            total = total.add((BigDecimal) row[1]);
        }

        List<Map<String, Object>> categories = new ArrayList<>();
        for (Object[] row : results) {
            Expense.Category category = (Expense.Category) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0;

            Map<String, Object> cat = new LinkedHashMap<>();
            cat.put("category", category.name());
            cat.put("amount", amount);
            cat.put("percentage", percentage);
            categories.add(cat);
        }

        breakdown.put("total", total);
        breakdown.put("categories", categories);
        breakdown.put("period", months + " months");

        return breakdown;
    }

    public Map<String, Object> getSpendingInsights(Long userId) {
        Map<String, Object> insights = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate startOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfLastMonth = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        BigDecimal monthlyExpenses = expenseRepository.getTotalExpensesByDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal lastMonthExpenses = expenseRepository.getTotalExpensesByDateRange(userId, startOfLastMonth, endOfLastMonth);

        if (monthlyExpenses == null) monthlyExpenses = BigDecimal.ZERO;
        if (lastMonthExpenses == null) lastMonthExpenses = BigDecimal.ZERO;

        Expense.Category topCategory = expenseRepository.getTopSpendingCategory(userId, startOfMonth, endOfMonth);
        BigDecimal topCategoryAmount = topCategory != null
                ? expenseRepository.getTotalByCategoryAndDateRange(userId, topCategory, startOfMonth, endOfMonth)
                : BigDecimal.ZERO;

        insights.put("currentMonthSpending", monthlyExpenses);
        insights.put("lastMonthSpending", lastMonthExpenses);
        insights.put("topCategory", topCategory != null ? topCategory.name() : "N/A");
        insights.put("topCategoryAmount", topCategoryAmount != null ? topCategoryAmount : BigDecimal.ZERO);
        insights.put("averageDailySpending", monthlyExpenses.compareTo(BigDecimal.ZERO) > 0
                ? monthlyExpenses.divide(BigDecimal.valueOf(now.getDayOfMonth()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        insights.put("projectedMonthlySpending", monthlyExpenses.compareTo(BigDecimal.ZERO) > 0
                ? monthlyExpenses.divide(BigDecimal.valueOf(now.getDayOfMonth()), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(now.lengthOfMonth()))
                : BigDecimal.ZERO);

        return insights;
    }
}
