package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.dto.CustomerDTO;
import com.alemediaz.clearbooksbackend.models.Customer;
import com.alemediaz.clearbooksbackend.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerDTO> getAllCustomersByUserId(Long userId) {
        return customerRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return convertToDto(customer);
    }

    public CustomerDTO createCustomer(CustomerDTO customerDto, Long userId) {
        try {
            // Validate input
            if (customerDto.getName() == null || customerDto.getName().trim().isEmpty()) {
                throw new RuntimeException("Customer name is required");
            }
            if (customerDto.getVatNumber() == null || customerDto.getVatNumber().trim().isEmpty()) {
                throw new RuntimeException("VAT number is required");
            }

            Customer customer = new Customer();
            customer.setName(customerDto.getName().trim());
            customer.setVatNumber(customerDto.getVatNumber().trim());
            customer.setPhone(customerDto.getPhone() != null ? customerDto.getPhone().trim() : null);
            customer.setAddress(customerDto.getAddress() != null ? customerDto.getAddress().trim() : null);
            customer.setUserId(userId);

            Customer saved = customerRepository.save(customer);
            return convertToDto(saved);
        } catch (Exception e) {
            throw new RuntimeException("Error creating customer: " + e.getMessage(), e);
        }
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDto, Long userId) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        customer.setName(customerDto.getName());
        customer.setVatNumber(customerDto.getVatNumber());
        customer.setPhone(customerDto.getPhone());
        customer.setAddress(customerDto.getAddress());

        Customer updated = customerRepository.save(customer);
        return convertToDto(updated);
    }

    public void deleteCustomer(Long id, Long userId) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        customerRepository.delete(customer);
    }

    private CustomerDTO convertToDto(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setVatNumber(customer.getVatNumber());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        return dto;
    }
}
