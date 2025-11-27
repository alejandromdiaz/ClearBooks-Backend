package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String receiptPhoto; // Base64 encoded image
    private String notes;
}
