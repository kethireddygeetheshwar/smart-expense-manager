package com.expense.manager.config;

import com.expense.manager.entity.Expense;
import com.expense.manager.entity.User;
import com.expense.manager.repository.ExpenseRepository;
import com.expense.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User demo = User.builder()
                .email("demo@expense.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Demo User")
                .role(User.Role.USER)
                .build();
        userRepository.save(demo);

        LocalDate today = LocalDate.now();

        expenseRepository.save(createExpense(demo, "Monthly Salary", new BigDecimal("50000"),
                today.withDayOfMonth(1), Expense.Category.OTHER_INCOME, Expense.TransactionType.INCOME, Expense.PaymentMethod.NET_BANKING));
        expenseRepository.save(createExpense(demo, "Grocery Shopping", new BigDecimal("3500"),
                today.minusDays(2), Expense.Category.FOOD, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.UPI));
        expenseRepository.save(createExpense(demo, "Restaurant Dinner", new BigDecimal("1800"),
                today.minusDays(3), Expense.Category.FOOD, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.CREDIT_CARD));
        expenseRepository.save(createExpense(demo, "Uber to Office", new BigDecimal("450"),
                today.minusDays(1), Expense.Category.TRANSPORTATION, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.WALLET));
        expenseRepository.save(createExpense(demo, "Online Course", new BigDecimal("2999"),
                today.minusDays(5), Expense.Category.EDUCATION, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.DEBIT_CARD));
        expenseRepository.save(createExpense(demo, "Amazon Shopping", new BigDecimal("4200"),
                today.minusDays(4), Expense.Category.SHOPPING, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.CREDIT_CARD));
        expenseRepository.save(createExpense(demo, "Electricity Bill", new BigDecimal("1200"),
                today.minusDays(6), Expense.Category.UTILITIES, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.NET_BANKING));
        expenseRepository.save(createExpense(demo, "Movie Tickets", new BigDecimal("600"),
                today.minusDays(7), Expense.Category.ENTERTAINMENT, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.UPI));
        expenseRepository.save(createExpense(demo, "Flight Tickets", new BigDecimal("5500"),
                today.minusDays(10), Expense.Category.TRAVEL, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.CREDIT_CARD));
        expenseRepository.save(createExpense(demo, "Rent Payment", new BigDecimal("12000"),
                today.withDayOfMonth(5), Expense.Category.RENT, Expense.TransactionType.EXPENSE, Expense.PaymentMethod.NET_BANKING));

        expenseRepository.save(createRecurringExpense(demo, "Netflix Subscription", new BigDecimal("649"),
                today.minusDays(3), Expense.Category.ENTERTAINMENT, Expense.PaymentMethod.CREDIT_CARD));
        expenseRepository.save(createRecurringExpense(demo, "Spotify Premium", new BigDecimal("119"),
                today.minusDays(5), Expense.Category.ENTERTAINMENT, Expense.PaymentMethod.UPI));
        expenseRepository.save(createRecurringExpense(demo, "Internet Bill", new BigDecimal("799"),
                today.minusDays(7), Expense.Category.UTILITIES, Expense.PaymentMethod.NET_BANKING));
        expenseRepository.save(createRecurringExpense(demo, "Gym Membership", new BigDecimal("1500"),
                today.minusDays(2), Expense.Category.HEALTHCARE, Expense.PaymentMethod.DEBIT_CARD));
    }

    private Expense createExpense(User user, String desc, BigDecimal amount, LocalDate date,
                                   Expense.Category category, Expense.TransactionType type,
                                   Expense.PaymentMethod method) {
        return Expense.builder()
                .description(desc)
                .amount(amount)
                .date(date)
                .category(category)
                .type(type)
                .paymentMethod(method)
                .user(user)
                .build();
    }

    private Expense createRecurringExpense(User user, String desc, BigDecimal amount, LocalDate date,
                                            Expense.Category category, Expense.PaymentMethod method) {
        return Expense.builder()
                .description(desc)
                .amount(amount)
                .date(date)
                .category(category)
                .type(Expense.TransactionType.EXPENSE)
                .paymentMethod(method)
                .recurring(true)
                .recurrenceFrequency(Expense.RecurrenceFrequency.MONTHLY)
                .user(user)
                .build();
    }
}
