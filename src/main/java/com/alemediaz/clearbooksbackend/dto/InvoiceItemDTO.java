package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemDTO {
    private Long id;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
