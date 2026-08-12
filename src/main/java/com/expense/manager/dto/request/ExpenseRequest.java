package com.expense.manager.dto.request;

import com.expense.manager.entity.Expense;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Category is required")
    private Expense.Category category;

    @NotNull(message = "Transaction type is required")
    private Expense.TransactionType type;

    private Expense.PaymentMethod paymentMethod;

    private boolean recurring;

    private Expense.RecurrenceFrequency recurrenceFrequency;

    private String notes;
}
