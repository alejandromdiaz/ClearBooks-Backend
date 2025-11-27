package com.alemediaz.clearbooksbackend.controllers;

import com.alemediaz.clearbooksbackend.dto.ExpenseDTO;
import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.services.ExpenseService;
import com.alemediaz.clearbooksbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(expenseService.getAllExpensesByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpense(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(expenseService.getExpenseById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createExpense(
            @RequestBody ExpenseDTO expenseDto,
            Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            ExpenseDTO created = expenseService.createExpense(expenseDto, user.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating expense: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(
            @PathVariable Long id,
            @RequestBody ExpenseDTO expenseDto,
            Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            ExpenseDTO updated = expenseService.updateExpense(id, expenseDto, user.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating expense: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            expenseService.deleteExpense(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting expense: " + e.getMessage());
        }
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalExpenses(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        BigDecimal total = expenseService.getTotalExpenses(user.getId());
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/range")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(user.getId(), startDate, endDate));
    }

    @GetMapping("/range/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        BigDecimal total = expenseService.getTotalExpensesByDateRange(user.getId(), startDate, endDate);
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }
}
