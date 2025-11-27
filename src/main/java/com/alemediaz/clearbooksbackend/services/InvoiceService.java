package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.dto.InvoiceDTO;
import com.alemediaz.clearbooksbackend.dto.InvoiceItemDTO;
import com.alemediaz.clearbooksbackend.models.Customer;
import com.alemediaz.clearbooksbackend.models.Estimate;
import com.alemediaz.clearbooksbackend.models.Invoice;
import com.alemediaz.clearbooksbackend.models.InvoiceItem;
import com.alemediaz.clearbooksbackend.repositories.CustomerRepository;
import com.alemediaz.clearbooksbackend.repositories.EstimateRepository;
import com.alemediaz.clearbooksbackend.repositories.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EstimateRepository estimateRepository;

    public List<InvoiceDTO> getAllInvoicesByUserId(Long userId) {
        return invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public InvoiceDTO getInvoiceById(Long id, Long userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return convertToDto(invoice);
    }

    @Transactional
    public InvoiceDTO createInvoice(InvoiceDTO invoiceDto, Long userId) {
        Customer customer = customerRepository.findById(invoiceDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateUniqueInvoiceNumber(userId));
        invoice.setCustomer(customer);
        invoice.setUserId(userId);
        invoice.setInvoiceDate(invoiceDto.getInvoiceDate() != null ?
                invoiceDto.getInvoiceDate() : LocalDate.now());
        invoice.setDueDate(invoiceDto.getDueDate());
        invoice.setSubtotal(invoiceDto.getSubtotal());
        invoice.setTaxRate(invoiceDto.getTaxRate());
        invoice.setTaxAmount(invoiceDto.getTaxAmount());
        invoice.setTotal(invoiceDto.getTotal());
        invoice.setNotes(invoiceDto.getNotes());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        if (invoiceDto.getItems() != null) {
            for (InvoiceItemDTO itemDto : invoiceDto.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoice(savedInvoice);
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setAmount(itemDto.getAmount());
                savedInvoice.getItems().add(item);
            }
            savedInvoice = invoiceRepository.save(savedInvoice);
        }

        return convertToDto(savedInvoice);
    }

    @Transactional
    public InvoiceDTO updateInvoice(Long id, InvoiceDTO invoiceDto, Long userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        Customer customer = customerRepository.findById(invoiceDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        invoice.setCustomer(customer);
        invoice.setInvoiceDate(invoiceDto.getInvoiceDate());
        invoice.setDueDate(invoiceDto.getDueDate());
        invoice.setSubtotal(invoiceDto.getSubtotal());
        invoice.setTaxRate(invoiceDto.getTaxRate());
        invoice.setTaxAmount(invoiceDto.getTaxAmount());
        invoice.setTotal(invoiceDto.getTotal());
        invoice.setNotes(invoiceDto.getNotes());

        invoice.getItems().clear();

        if (invoiceDto.getItems() != null) {
            for (InvoiceItemDTO itemDto : invoiceDto.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoice(invoice);
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setAmount(itemDto.getAmount());
                invoice.getItems().add(item);
            }
        }

        Invoice updated = invoiceRepository.save(invoice);
        return convertToDto(updated);
    }

    public void deleteInvoice(Long id, Long userId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        invoiceRepository.delete(invoice);
    }

    @Transactional
    public InvoiceDTO convertEstimateToInvoice(Long estimateId, Long userId) {
        try {
            System.out.println("=== Converting Estimate to Invoice ===");
            System.out.println("Estimate ID: " + estimateId);
            System.out.println("User ID: " + userId);

            // Find the estimate
            Estimate estimate = estimateRepository.findById(estimateId)
                    .orElseThrow(() -> new RuntimeException("Estimate not found with ID: " + estimateId));

            System.out.println("Found estimate: " + estimate.getEstimateNumber());

            if (!estimate.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized: Estimate does not belong to user");
            }

            // Create invoice from estimate
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(generateUniqueInvoiceNumber(userId));
            invoice.setCustomer(estimate.getCustomer());
            invoice.setUserId(userId);
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setDueDate(null);
            invoice.setSubtotal(estimate.getSubtotal());
            invoice.setTaxRate(estimate.getTaxRate());
            invoice.setTaxAmount(estimate.getTaxAmount());
            invoice.setTotal(estimate.getTotal());
            invoice.setNotes("Converted from estimate: " + estimate.getEstimateNumber() +
                    (estimate.getNotes() != null ? "\n\n" + estimate.getNotes() : ""));
            invoice.setIsPaid(false);

            System.out.println("Created invoice with number: " + invoice.getInvoiceNumber());

            Invoice savedInvoice = invoiceRepository.save(invoice);

            System.out.println("Saved invoice with ID: " + savedInvoice.getId());

            // Copy items
            for (InvoiceItem estimateItem : estimate.getItems()) {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setInvoice(savedInvoice);
                invoiceItem.setDescription(estimateItem.getDescription());
                invoiceItem.setQuantity(estimateItem.getQuantity());
                invoiceItem.setUnitPrice(estimateItem.getUnitPrice());
                invoiceItem.setAmount(estimateItem.getAmount());
                savedInvoice.getItems().add(invoiceItem);
            }

            savedInvoice = invoiceRepository.save(savedInvoice);

            System.out.println("Saved invoice with " + savedInvoice.getItems().size() + " items");

            // Delete the estimate
            estimateRepository.delete(estimate);

            System.out.println("Deleted estimate");
            System.out.println("=== Conversion Complete ===");

            return convertToDto(savedInvoice);
        } catch (Exception e) {
            System.err.println("=== ERROR Converting Estimate ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to convert estimate: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a unique invoice number with retry logic to handle race conditions
     */
    private String generateUniqueInvoiceNumber(Long userId) {
        int maxRetries = 10;
        int attempt = 0;

        while (attempt < maxRetries) {
            String invoiceNumber = generateInvoiceNumber(userId, attempt);

            // Check if this number already exists
            if (!invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
                return invoiceNumber;
            }

            attempt++;
        }

        throw new RuntimeException("Could not generate unique invoice number after " + maxRetries + " attempts");
    }

    /**
     * Generates invoice number based on the last invoice for the user
     */
    private String generateInvoiceNumber(Long userId, int offset) {
        List<Invoice> invoices = invoiceRepository.findTopByUserIdOrderByInvoiceNumberDesc(userId);
        int currentYear = LocalDate.now().getYear();

        if (invoices.isEmpty()) {
            return String.format("INV-%d-%04d", currentYear, 1 + offset);
        }

        String lastNumber = invoices.get(0).getInvoiceNumber();
        String[] parts = lastNumber.split("-");

        // Check if last invoice was from current year
        int lastYear = Integer.parseInt(parts[1]);
        int lastSequence = Integer.parseInt(parts[2]);

        int nextNumber;
        if (lastYear == currentYear) {
            nextNumber = lastSequence + 1 + offset;
        } else {
            // New year, reset to 1
            nextNumber = 1 + offset;
        }

        return String.format("INV-%d-%04d", currentYear, nextNumber);
    }

    private InvoiceDTO convertToDto(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerId(invoice.getCustomer().getId());
        dto.setCustomerName(invoice.getCustomer().getName());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxRate(invoice.getTaxRate());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotal(invoice.getTotal());
        dto.setNotes(invoice.getNotes());
        dto.setIsPaid(invoice.getIsPaid());
        dto.setPaidDate(invoice.getPaidDate());

        // Add user details
        if (invoice.getUser() != null) {
            dto.setUserCompanyName(invoice.getUser().getCompanyName());
            dto.setUserAddress(invoice.getUser().getAddress());
            dto.setUserVatNumber(invoice.getUser().getVatNumber());
            dto.setUserPhone(invoice.getUser().getPhoneNumber());
            dto.setUserEmail(invoice.getUser().getEmail());
        }

        if (invoice.getItems() != null) {
            dto.setItems(invoice.getItems().stream()
                    .map(this::convertItemToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private InvoiceItemDTO convertItemToDto(InvoiceItem item) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        dto.setId(item.getId());
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setAmount(item.getAmount());
        return dto;
    }
}