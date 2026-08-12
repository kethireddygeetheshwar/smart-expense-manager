package com.expense.manager.service;

import com.expense.manager.dto.request.ExpenseRequest;
import com.expense.manager.dto.response.ExpenseResponse;
import com.expense.manager.entity.Expense;
import com.expense.manager.entity.User;
import com.expense.manager.exception.ResourceNotFoundException;
import com.expense.manager.repository.ExpenseRepository;
import com.expense.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponse createExpense(Long userId, ExpenseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .type(request.getType())
                .paymentMethod(request.getPaymentMethod())
                .recurring(request.isRecurring())
                .recurrenceFrequency(request.getRecurrenceFrequency())
                .notes(request.getNotes())
                .user(user)
                .build();

        expense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(expense);
    }

    public List<ExpenseResponse> getAllExpenses(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId).stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getExpenseById(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (!expense.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long userId, Long expenseId, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (!expense.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setCategory(request.getCategory());
        expense.setType(request.getType());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setRecurring(request.isRecurring());
        expense.setRecurrenceFrequency(request.getRecurrenceFrequency());
        expense.setNotes(request.getNotes());

        expense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public void deleteExpense(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (!expense.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        expenseRepository.delete(expense);
    }

    public List<ExpenseResponse> searchExpenses(Long userId, String search) {
        return expenseRepository.searchExpenses(userId, search).stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByCategory(Long userId, Expense.Category category) {
        return expenseRepository.findByUserIdAndCategory(userId, category).stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
