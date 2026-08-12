package com.expense.manager.dto.response;

import com.expense.manager.entity.Goal;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class GoalResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private double progressPercentage;
    private BigDecimal remainingAmount;
    private LocalDate targetDate;
    private int monthsRemaining;
    private BigDecimal recommendedMonthlySaving;
    private boolean completed;
    private String icon;

    public static GoalResponse fromEntity(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .progressPercentage(goal.getProgressPercentage())
                .remainingAmount(goal.getRemainingAmount())
                .targetDate(goal.getTargetDate())
                .monthsRemaining(goal.getMonthsRemaining())
                .recommendedMonthlySaving(goal.getRecommendedMonthlySaving())
                .completed(goal.isCompleted())
                .icon(goal.getIcon())
                .build();
    }
}
