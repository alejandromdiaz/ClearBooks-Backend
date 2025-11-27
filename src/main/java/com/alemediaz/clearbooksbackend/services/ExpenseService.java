package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.dto.ExpenseDTO;
import com.alemediaz.clearbooksbackend.models.Expense;
import com.alemediaz.clearbooksbackend.repositories.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;

    public List<ExpenseDTO> getAllExpensesByUserId(Long userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getExpenseById(Long id, Long userId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return convertToDto(expense);
    }

    @Transactional
    public ExpenseDTO createExpense(ExpenseDTO expenseDto, Long userId) {
        try {
            // Validate input
            if (expenseDto.getName() == null || expenseDto.getName().trim().isEmpty()) {
                throw new RuntimeException("Expense name is required");
            }
            if (expenseDto.getAmount() == null || expenseDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Amount must be greater than zero");
            }

            Expense expense = new Expense();
            expense.setName(expenseDto.getName().trim());
            expense.setAmount(expenseDto.getAmount());
            expense.setExpenseDate(expenseDto.getExpenseDate() != null ?
                    expenseDto.getExpenseDate() : LocalDate.now());
            expense.setReceiptPhoto(expenseDto.getReceiptPhoto());
            expense.setNotes(expenseDto.getNotes());
            expense.setUserId(userId);

            Expense saved = expenseRepository.save(expense);
            return convertToDto(saved);
        } catch (Exception e) {
            throw new RuntimeException("Error creating expense: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDto, Long userId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        expense.setName(expenseDto.getName().trim());
        expense.setAmount(expenseDto.getAmount());
        expense.setExpenseDate(expenseDto.getExpenseDate());
        expense.setReceiptPhoto(expenseDto.getReceiptPhoto());
        expense.setNotes(expenseDto.getNotes());

        Expense updated = expenseRepository.save(expense);
        return convertToDto(updated);
    }

    public void deleteExpense(Long id, Long userId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        expenseRepository.delete(expense);
    }

    public BigDecimal getTotalExpenses(Long userId) {
        BigDecimal total = expenseRepository.getTotalExpensesByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<ExpenseDTO> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = expenseRepository.getTotalExpensesByUserIdAndDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    private ExpenseDTO convertToDto(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setName(expense.getName());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setReceiptPhoto(expense.getReceiptPhoto());
        dto.setNotes(expense.getNotes());
        return dto;
    }
}
