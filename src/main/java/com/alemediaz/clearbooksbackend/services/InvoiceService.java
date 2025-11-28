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
        invoice.setInvoiceNumber(generateUniqueInvoiceNumberForUser(userId));
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

            Estimate estimate = estimateRepository.findById(estimateId)
                    .orElseThrow(() -> new RuntimeException("Estimate not found with ID: " + estimateId));

            System.out.println("Found estimate: " + estimate.getEstimateNumber());

            if (!estimate.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized: Estimate does not belong to user");
            }

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(generateUniqueInvoiceNumberForUser(userId));
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
     * Generates a unique invoice number for a specific user
     */
    private String generateUniqueInvoiceNumberForUser(Long userId) {
        int maxRetries = 10;
        int attempt = 0;

        while (attempt < maxRetries) {
            String invoiceNumber = generateInvoiceNumberForUser(userId, attempt);

            System.out.println("DEBUG: Attempt " + attempt + " - Generated: " + invoiceNumber + " for userId: " + userId);

            if (!invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
                System.out.println("DEBUG: Invoice number " + invoiceNumber + " is unique");
                return invoiceNumber;
            }

            System.out.println("DEBUG: Invoice number " + invoiceNumber + " already exists, retrying...");
            attempt++;
        }

        throw new RuntimeException("Could not generate unique invoice number after " + maxRetries + " attempts");
    }

    /**
     * Generates invoice number based on THIS user's invoices only
     */
    private String generateInvoiceNumberForUser(Long userId, int offset) {
        // Get ONLY this user's invoices
        List<Invoice> userInvoices = invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId);
        int currentYear = LocalDate.now().getYear();

        System.out.println("DEBUG: User " + userId + " has " + userInvoices.size() + " total invoices");

        if (userInvoices.isEmpty()) {
            String number = String.format("INV-%d-%04d", currentYear, 1 + offset);
            System.out.println("DEBUG: First invoice for user, generating: " + number);
            return number;
        }

        // Find the highest sequence number for the current year FOR THIS USER
        int highestSequence = 0;
        for (Invoice inv : userInvoices) {
            String invNumber = inv.getInvoiceNumber();
            System.out.println("DEBUG: Checking user invoice: " + invNumber);

            try {
                String[] parts = invNumber.split("-");
                if (parts.length == 3) {
                    int invYear = Integer.parseInt(parts[1]);
                    int invSequence = Integer.parseInt(parts[2]);

                    if (invYear == currentYear && invSequence > highestSequence) {
                        highestSequence = invSequence;
                        System.out.println("DEBUG: New highest sequence for current year: " + highestSequence);
                    }
                }
            } catch (Exception e) {
                System.err.println("DEBUG: Error parsing invoice number: " + invNumber);
            }
        }

        int nextNumber = highestSequence + 1 + offset;
        String invoiceNumber = String.format("INV-%d-%04d", currentYear, nextNumber);
        System.out.println("DEBUG: Next number: " + nextNumber + " -> " + invoiceNumber);

        return invoiceNumber;
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