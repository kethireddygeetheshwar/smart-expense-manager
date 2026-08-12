package com.expense.manager.controller;

import com.expense.manager.dto.request.BudgetRequest;
import com.expense.manager.dto.response.BudgetResponse;
import com.expense.manager.dto.response.MessageResponse;
import com.expense.manager.service.BudgetService;
import com.expense.manager.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final UserContext userContext;

    @PostMapping
    public ResponseEntity<BudgetResponse> createOrUpdateBudget(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(userContext.getCurrentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getUserBudgets() {
        return ResponseEntity.ok(budgetService.getUserBudgets(userContext.getCurrentUserId()));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<BudgetResponse>> getBudgetAlerts() {
        return ResponseEntity.ok(budgetService.getBudgetAlerts(userContext.getCurrentUserId()));
    }

    @PostMapping("/refresh/{monthYear}")
    public ResponseEntity<List<BudgetResponse>> refreshBudgetSpending(@PathVariable String monthYear) {
        return ResponseEntity.ok(budgetService.refreshBudgetSpending(userContext.getCurrentUserId(), monthYear));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(userContext.getCurrentUserId(), id);
        return ResponseEntity.ok(new MessageResponse("Budget deleted successfully"));
    }
}
