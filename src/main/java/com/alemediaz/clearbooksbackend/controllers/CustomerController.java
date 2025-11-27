package com.alemediaz.clearbooksbackend.controllers;

import com.alemediaz.clearbooksbackend.dto.CustomerDTO;
import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.services.CustomerService;
import com.alemediaz.clearbooksbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(customerService.getAllCustomersByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(
            @RequestBody CustomerDTO customerDto,
            Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            CustomerDTO created = customerService.createCustomer(customerDto, user.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating customer: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerDTO customerDto,
            Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(customerService.updateCustomer(id, customerDto, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        customerService.deleteCustomer(id, user.getId());
        return ResponseEntity.ok().build();
    }
}
