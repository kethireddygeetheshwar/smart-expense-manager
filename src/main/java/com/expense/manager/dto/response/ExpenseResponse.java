package com.expense.manager.dto.response;

import com.expense.manager.entity.Expense;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private String type;
    private String paymentMethod;
    private boolean recurring;
    private String recurrenceFrequency;
    private String notes;
    private LocalDateTime createdAt;

    public static ExpenseResponse fromEntity(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .category(expense.getCategory().name())
                .type(expense.getType().name())
                .paymentMethod(expense.getPaymentMethod() != null ? expense.getPaymentMethod().name() : null)
                .recurring(expense.isRecurring())
                .recurrenceFrequency(expense.getRecurrenceFrequency() != null ? expense.getRecurrenceFrequency().name() : null)
                .notes(expense.getNotes())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
