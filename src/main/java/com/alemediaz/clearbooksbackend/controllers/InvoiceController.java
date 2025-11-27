package com.alemediaz.clearbooksbackend.controllers;

import com.alemediaz.clearbooksbackend.dto.InvoiceDTO;
import com.alemediaz.clearbooksbackend.models.Invoice;
import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.repositories.InvoiceRepository;
import com.alemediaz.clearbooksbackend.services.InvoiceService;
import com.alemediaz.clearbooksbackend.services.PDFService;
import com.alemediaz.clearbooksbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserService userService;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(invoiceService.getAllInvoicesByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(invoiceService.getInvoiceById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(
            @RequestBody InvoiceDTO invoiceDto,
            Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(invoiceService.createInvoice(invoiceDto, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(
            @PathVariable Long id,
            @RequestBody InvoiceDTO invoiceDto,
            Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoiceDto, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        invoiceService.deleteInvoice(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdfBytes = pdfService.generateInvoicePdf(invoice);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "invoice-" + invoice.getInvoiceNumber() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PatchMapping("/{id}/paid")
    public ResponseEntity<?> togglePaidStatus(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            if (!invoice.getUserId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized");
            }

            invoice.setIsPaid(!invoice.getIsPaid());
            if (invoice.getIsPaid()) {
                invoice.setPaidDate(java.time.LocalDate.now());
            } else {
                invoice.setPaidDate(null);
            }

            Invoice savedInvoice = invoiceRepository.save(invoice);

            Map<String, Object> response = new HashMap<>();
            response.put("isPaid", savedInvoice.getIsPaid());
            response.put("paidDate", savedInvoice.getPaidDate() != null ? savedInvoice.getPaidDate().toString() : null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating paid status: " + e.getMessage());
        }
    }
}
