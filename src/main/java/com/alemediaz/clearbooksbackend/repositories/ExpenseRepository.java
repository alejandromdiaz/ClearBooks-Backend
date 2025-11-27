package com.alemediaz.clearbooksbackend.repositories;

import com.alemediaz.clearbooksbackend.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    @Query("SELECT e FROM Expense e WHERE e.userId = ?1 AND e.expenseDate BETWEEN ?2 AND ?3 ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = ?1")
    BigDecimal getTotalExpensesByUserId(Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = ?1 AND e.expenseDate BETWEEN ?2 AND ?3")
    BigDecimal getTotalExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}
