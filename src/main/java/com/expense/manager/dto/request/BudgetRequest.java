package com.expense.manager.dto.request;

import com.expense.manager.entity.Expense;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotNull(message = "Category is required")
    private Expense.Category category;

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit must be positive")
    private BigDecimal limitAmount;

    @NotNull(message = "Month-Year is required")
    private String monthYear;
}
