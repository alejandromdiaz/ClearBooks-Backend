package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.dto.EstimateDTO;
import com.alemediaz.clearbooksbackend.dto.InvoiceItemDTO;
import com.alemediaz.clearbooksbackend.models.Customer;
import com.alemediaz.clearbooksbackend.models.Estimate;
import com.alemediaz.clearbooksbackend.models.InvoiceItem;
import com.alemediaz.clearbooksbackend.repositories.CustomerRepository;
import com.alemediaz.clearbooksbackend.repositories.EstimateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstimateService {
    @Autowired
    private EstimateRepository estimateRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public List<EstimateDTO> getAllEstimatesByUserId(Long userId) {
        return estimateRepository.findByUserIdOrderByEstimateDateDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public EstimateDTO getEstimateById(Long id, Long userId) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found"));

        if (!estimate.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return convertToDto(estimate);
    }

    @Transactional
    public EstimateDTO createEstimate(EstimateDTO estimateDto, Long userId) {
        Customer customer = customerRepository.findById(estimateDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Estimate estimate = new Estimate();
        estimate.setEstimateNumber(generateEstimateNumber(userId));
        estimate.setCustomer(customer);
        estimate.setUserId(userId);
        estimate.setEstimateDate(estimateDto.getEstimateDate() != null ?
                estimateDto.getEstimateDate() : LocalDate.now());
        estimate.setValidUntil(estimateDto.getValidUntil());
        estimate.setSubtotal(estimateDto.getSubtotal());
        estimate.setTaxRate(estimateDto.getTaxRate());
        estimate.setTaxAmount(estimateDto.getTaxAmount());
        estimate.setTotal(estimateDto.getTotal());
        estimate.setNotes(estimateDto.getNotes());

        Estimate savedEstimate = estimateRepository.save(estimate);

        if (estimateDto.getItems() != null) {
            for (InvoiceItemDTO itemDto : estimateDto.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setEstimate(savedEstimate);
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setAmount(itemDto.getAmount());
                savedEstimate.getItems().add(item);
            }
            savedEstimate = estimateRepository.save(savedEstimate);
        }

        return convertToDto(savedEstimate);
    }

    @Transactional
    public EstimateDTO updateEstimate(Long id, EstimateDTO estimateDto, Long userId) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found"));

        if (!estimate.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        Customer customer = customerRepository.findById(estimateDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        estimate.setCustomer(customer);
        estimate.setEstimateDate(estimateDto.getEstimateDate());
        estimate.setValidUntil(estimateDto.getValidUntil());
        estimate.setSubtotal(estimateDto.getSubtotal());
        estimate.setTaxRate(estimateDto.getTaxRate());
        estimate.setTaxAmount(estimateDto.getTaxAmount());
        estimate.setTotal(estimateDto.getTotal());
        estimate.setNotes(estimateDto.getNotes());

        estimate.getItems().clear();

        if (estimateDto.getItems() != null) {
            for (InvoiceItemDTO itemDto : estimateDto.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setEstimate(estimate);
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setAmount(itemDto.getAmount());
                estimate.getItems().add(item);
            }
        }

        Estimate updated = estimateRepository.save(estimate);
        return convertToDto(updated);
    }

    public void deleteEstimate(Long id, Long userId) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found"));

        if (!estimate.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        estimateRepository.delete(estimate);
    }

    private String generateEstimateNumber(Long userId) {
        List<Estimate> estimates = estimateRepository.findTopByUserIdOrderByEstimateNumberDesc(userId);

        if (estimates.isEmpty()) {
            return "EST-" + LocalDate.now().getYear() + "-0001";
        }

        String lastNumber = estimates.get(0).getEstimateNumber();
        String[] parts = lastNumber.split("-");
        int number = Integer.parseInt(parts[2]) + 1;

        return String.format("EST-%d-%04d", LocalDate.now().getYear(), number);
    }

    private EstimateDTO convertToDto(Estimate estimate) {
        EstimateDTO dto = new EstimateDTO();
        dto.setId(estimate.getId());
        dto.setEstimateNumber(estimate.getEstimateNumber());
        dto.setCustomerId(estimate.getCustomer().getId());
        dto.setCustomerName(estimate.getCustomer().getName());
        dto.setEstimateDate(estimate.getEstimateDate());
        dto.setValidUntil(estimate.getValidUntil());
        dto.setSubtotal(estimate.getSubtotal());
        dto.setTaxRate(estimate.getTaxRate());
        dto.setTaxAmount(estimate.getTaxAmount());
        dto.setTotal(estimate.getTotal());
        dto.setNotes(estimate.getNotes());

        if (estimate.getItems() != null) {
            dto.setItems(estimate.getItems().stream()
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
