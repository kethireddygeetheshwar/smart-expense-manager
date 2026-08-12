package com.expense.manager.dto.response;

import com.expense.manager.entity.Budget;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BudgetResponse {
    private Long id;
    private String category;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private double usagePercentage;
    private String monthYear;
    private boolean nearLimit;
    private boolean exceeded;

    public static BudgetResponse fromEntity(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory().name())
                .limitAmount(budget.getLimitAmount())
                .spentAmount(budget.getSpentAmount())
                .usagePercentage(budget.getUsagePercentage())
                .monthYear(budget.getMonthYear())
                .nearLimit(budget.isNearLimit())
                .exceeded(budget.isExceeded())
                .build();
    }
}
