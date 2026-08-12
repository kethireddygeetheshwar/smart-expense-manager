package com.expense.manager.repository;

import com.expense.manager.entity.Budget;
import com.expense.manager.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByUserIdAndCategoryAndMonthYear(Long userId, Expense.Category category, String monthYear);

    List<Budget> findByUserIdAndMonthYear(Long userId, String monthYear);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.spentAmount >= b.limitAmount * 0.9")
    List<Budget> findBudgetsNearLimit(@Param("userId") Long userId);
}
