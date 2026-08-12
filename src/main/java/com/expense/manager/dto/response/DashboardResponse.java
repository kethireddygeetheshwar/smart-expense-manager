package com.expense.manager.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal remaining;
    private double savingsRate;
    private String topCategory;
    private BigDecimal topCategoryAmount;
    private List<CategorySpending> categorySpendings;
    private List<MonthlySpending> monthlySpendings;
    private List<BudgetAlert> budgetAlerts;

    @Data
    @Builder
    public static class CategorySpending {
        private String category;
        private BigDecimal amount;
        private double percentage;
    }

    @Data
    @Builder
    public static class MonthlySpending {
        private int month;
        private String monthName;
        private BigDecimal amount;
    }

    @Data
    @Builder
    public static class BudgetAlert {
        private String category;
        private BigDecimal limitAmount;
        private BigDecimal spentAmount;
        private double usagePercentage;
        private boolean exceeded;
    }
}
