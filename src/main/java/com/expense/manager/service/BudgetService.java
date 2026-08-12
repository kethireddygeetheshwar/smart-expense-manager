package com.expense.manager.service;

import com.expense.manager.dto.request.BudgetRequest;
import com.expense.manager.dto.response.BudgetResponse;
import com.expense.manager.entity.Budget;
import com.expense.manager.entity.Expense;
import com.expense.manager.entity.User;
import com.expense.manager.exception.ResourceNotFoundException;
import com.expense.manager.repository.BudgetRepository;
import com.expense.manager.repository.ExpenseRepository;
import com.expense.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetResponse createOrUpdateBudget(Long userId, BudgetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Budget budget = budgetRepository
                .findByUserIdAndCategoryAndMonthYear(userId, request.getCategory(), request.getMonthYear())
                .orElse(Budget.builder()
                        .category(request.getCategory())
                        .monthYear(request.getMonthYear())
                        .user(user)
                        .build());

        budget.setLimitAmount(request.getLimitAmount());

        LocalDate start = LocalDate.parse(request.getMonthYear() + "-01");
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal spent = expenseRepository.getTotalSpentByCategory(userId, request.getCategory(), start, end);
        budget.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);

        budget = budgetRepository.save(budget);
        return BudgetResponse.fromEntity(budget);
    }

    public List<BudgetResponse> getUserBudgets(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(BudgetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BudgetResponse> refreshBudgetSpending(Long userId, String monthYear) {
        LocalDate start = LocalDate.parse(monthYear + "-01");
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
        List<BudgetResponse> responses = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spent = expenseRepository.getTotalSpentByCategory(
                    userId, budget.getCategory(), start, end);
            budget.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);
            budget = budgetRepository.save(budget);
            responses.add(BudgetResponse.fromEntity(budget));
        }

        return responses;
    }

    public List<BudgetResponse> getBudgetAlerts(Long userId) {
        return budgetRepository.findBudgetsNearLimit(userId).stream()
                .map(BudgetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        budgetRepository.delete(budget);
    }
}
