package com.expense.manager.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {
    @NotNull(message = "Name is required")
    private String name;
    private String description;
    @NotNull(message = "Target amount is required")
    @Positive(message = "Target must be positive")
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private String icon;
}
