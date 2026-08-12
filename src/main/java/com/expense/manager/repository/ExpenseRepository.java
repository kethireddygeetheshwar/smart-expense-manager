package com.expense.manager.repository;

import com.expense.manager.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    List<Expense> findByUserIdAndCategory(Long userId, Expense.Category category);

    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = 'INCOME' AND e.date BETWEEN :start AND :end")
    BigDecimal getTotalIncomeByDateRange(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.date BETWEEN :start AND :end")
    BigDecimal getTotalExpensesByDateRange(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.category = :category AND e.date BETWEEN :start AND :end")
    BigDecimal getTotalByCategoryAndDateRange(@Param("userId") Long userId, @Param("category") Expense.Category category, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category, SUM(e.amount) as total FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.date BETWEEN :start AND :end GROUP BY e.category ORDER BY total DESC")
    List<Object[]> getCategorySpending(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT MONTH(e.date) as month, SUM(e.amount) as total FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND YEAR(e.date) = :year GROUP BY MONTH(e.date) ORDER BY month")
    List<Object[]> getMonthlySpending(@Param("userId") Long userId, @Param("year") int year);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY e.date DESC")
    List<Expense> searchExpenses(@Param("userId") Long userId, @Param("search") String search);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.category = :category AND e.date BETWEEN :start AND :end")
    BigDecimal getTotalSpentByCategory(@Param("userId") Long userId, @Param("category") Expense.Category category, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.date BETWEEN :start AND :end GROUP BY e.category ORDER BY SUM(e.amount) DESC LIMIT 1")
    Expense.Category getTopSpendingCategory(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category, AVG(e.amount) as avg_amount, STDDEV(e.amount) as std_dev FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.date BETWEEN :start AND :end GROUP BY e.category")
    List<Object[]> getMonthlyAvgByCategory(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category, SUM(e.amount) as total FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.date BETWEEN :start AND :end GROUP BY e.category ORDER BY total DESC")
    List<Object[]> getCategoryBreakdown(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.description, e.amount, e.date FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.recurring = true ORDER BY e.date DESC")
    List<Expense> findRecurringExpenses(@Param("userId") Long userId);
}
