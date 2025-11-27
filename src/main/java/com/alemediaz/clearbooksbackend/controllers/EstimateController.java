package com.alemediaz.clearbooksbackend.controllers;

import com.alemediaz.clearbooksbackend.dto.EstimateDTO;
import com.alemediaz.clearbooksbackend.dto.InvoiceDTO;
import com.alemediaz.clearbooksbackend.models.Estimate;
import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.repositories.EstimateRepository;
import com.alemediaz.clearbooksbackend.services.EstimateService;
import com.alemediaz.clearbooksbackend.services.InvoiceService;
import com.alemediaz.clearbooksbackend.services.PDFService;
import com.alemediaz.clearbooksbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/estimates")
public class EstimateController {
    @Autowired
    private EstimateService estimateService;

    @Autowired
    private UserService userService;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private EstimateRepository estimateRepository;

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<EstimateDTO>> getAllEstimates(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(estimateService.getAllEstimatesByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstimateDTO> getEstimate(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(estimateService.getEstimateById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createEstimate(
            @RequestBody EstimateDTO estimateDto,
            Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            EstimateDTO created = estimateService.createEstimate(estimateDto, user.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating estimate: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEstimate(
            @PathVariable Long id,
            @RequestBody EstimateDTO estimateDto,
            Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            EstimateDTO updated = estimateService.updateEstimate(id, estimateDto, user.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating estimate: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEstimate(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            estimateService.deleteEstimate(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting estimate: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadEstimatePdf(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found"));

        if (!estimate.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdfBytes = pdfService.generateEstimatePdf(estimate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "estimate-" + estimate.getEstimateNumber() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/{id}/convert-to-invoice")
    public ResponseEntity<?> convertToInvoice(@PathVariable Long id, Authentication authentication) {
        System.out.println("=== CONVERT ENDPOINT CALLED ===");
        System.out.println("Estimate ID: " + id);
        System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "NULL"));

        try {
            if (authentication == null) {
                System.err.println("ERROR: No authentication provided");
                return ResponseEntity.status(401).body("Not authenticated");
            }

            User user = userService.findByVatNumber(authentication.getName());
            System.out.println("User ID: " + user.getId());

            InvoiceDTO invoice = invoiceService.convertEstimateToInvoice(id, user.getId());

            System.out.println("=== CONVERSION SUCCESSFUL ===");
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            System.err.println("=== RUNTIME ERROR ===");
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error converting estimate: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("=== GENERAL ERROR ===");
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
}

}