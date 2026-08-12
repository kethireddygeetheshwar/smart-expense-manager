package com.expense.manager.controller;

import com.expense.manager.dto.request.ExpenseRequest;
import com.expense.manager.dto.response.ExpenseResponse;
import com.expense.manager.dto.response.MessageResponse;
import com.expense.manager.entity.Expense;
import com.expense.manager.service.ExpenseService;
import com.expense.manager.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserContext userContext;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.createExpense(userContext.getCurrentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses(userContext.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(userContext.getCurrentUserId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(userContext.getCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(userContext.getCurrentUserId(), id);
        return ResponseEntity.ok(new MessageResponse("Expense deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ExpenseResponse>> searchExpenses(@RequestParam String q) {
        return ResponseEntity.ok(expenseService.searchExpenses(userContext.getCurrentUserId(), q));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExpenseResponse>> getByCategory(@PathVariable Expense.Category category) {
        return ResponseEntity.ok(expenseService.getExpensesByCategory(userContext.getCurrentUserId(), category));
    }
}
