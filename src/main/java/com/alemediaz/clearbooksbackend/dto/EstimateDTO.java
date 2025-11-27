package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EstimateDTO {
    private Long id;
    private String estimateNumber;
    private Long customerId;
    private String customerName;
    private LocalDate estimateDate;
    private LocalDate validUntil;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String notes;
    private List<InvoiceItemDTO> items;
}
