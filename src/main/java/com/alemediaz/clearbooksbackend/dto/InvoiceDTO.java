package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceDTO {
    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String notes;
    private Boolean isPaid;
    private LocalDate paidDate;
    private List<InvoiceItemDTO> items;

    private String userCompanyName;
    private String userAddress;
    private String userVatNumber;
    private String userPhone;
    private String userEmail;

    // Getters and Setters
    public String getUserCompanyName() {
        return userCompanyName;
    }

    public void setUserCompanyName(String userCompanyName) {
        this.userCompanyName = userCompanyName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserVatNumber() {
        return userVatNumber;
    }

    public void setUserVatNumber(String userVatNumber) {
        this.userVatNumber = userVatNumber;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
