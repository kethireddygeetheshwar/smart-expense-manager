package com.expense.manager.service;

import com.expense.manager.dto.response.BudgetResponse;
import com.expense.manager.dto.response.DashboardResponse;
import com.expense.manager.entity.Expense;
import com.expense.manager.repository.BudgetRepository;
import com.expense.manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public DashboardResponse getDashboard(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal totalIncome = expenseRepository.getTotalIncomeByDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(userId, startOfMonth, endOfMonth);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal remaining = totalIncome.subtract(totalExpenses);
        double savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? remaining.multiply(BigDecimal.valueOf(100)).divide(totalIncome, 2, RoundingMode.HALF_UP).doubleValue()
                : 0;

        Expense.Category topCategory = expenseRepository.getTopSpendingCategory(userId, startOfMonth, endOfMonth);
        BigDecimal topCategoryAmount = topCategory != null
                ? expenseRepository.getTotalByCategoryAndDateRange(userId, topCategory, startOfMonth, endOfMonth)
                : BigDecimal.ZERO;

        List<DashboardResponse.CategorySpending> categorySpendings = getCategorySpendings(userId, startOfMonth, endOfMonth, totalExpenses);
        List<DashboardResponse.MonthlySpending> monthlySpendings = getMonthlySpending(userId, now.getYear());
        List<DashboardResponse.BudgetAlert> budgetAlerts = getBudgetAlerts(userId);

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .remaining(remaining)
                .savingsRate(Math.max(savingsRate, 0))
                .topCategory(topCategory != null ? topCategory.name() : "N/A")
                .topCategoryAmount(topCategoryAmount != null ? topCategoryAmount : BigDecimal.ZERO)
                .categorySpendings(categorySpendings)
                .monthlySpendings(monthlySpendings)
                .budgetAlerts(budgetAlerts)
                .build();
    }

    private List<DashboardResponse.CategorySpending> getCategorySpendings(Long userId, LocalDate start, LocalDate end, BigDecimal totalExpenses) {
        List<Object[]> results = expenseRepository.getCategorySpending(userId, start, end);
        List<DashboardResponse.CategorySpending> spendings = new ArrayList<>();

        for (Object[] row : results) {
            Expense.Category category = (Expense.Category) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(totalExpenses, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0;

            spendings.add(DashboardResponse.CategorySpending.builder()
                    .category(category.name())
                    .amount(amount)
                    .percentage(percentage)
                    .build());
        }

        return spendings;
    }

    private List<DashboardResponse.MonthlySpending> getMonthlySpending(Long userId, int year) {
        List<Object[]> results = expenseRepository.getMonthlySpending(userId, year);
        List<DashboardResponse.MonthlySpending> spendings = new ArrayList<>();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (Object[] row : results) {
            int month = ((Number) row[0]).intValue();
            BigDecimal amount = (BigDecimal) row[1];

            spendings.add(DashboardResponse.MonthlySpending.builder()
                    .month(month)
                    .monthName(monthNames[month - 1])
                    .amount(amount)
                    .build());
        }

        return spendings;
    }

    private List<DashboardResponse.BudgetAlert> getBudgetAlerts(Long userId) {
        return budgetRepository.findBudgetsNearLimit(userId).stream()
                .map(budget -> DashboardResponse.BudgetAlert.builder()
                        .category(budget.getCategory().name())
                        .limitAmount(budget.getLimitAmount())
                        .spentAmount(budget.getSpentAmount())
                        .usagePercentage(budget.getUsagePercentage())
                        .exceeded(budget.isExceeded())
                        .build())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
