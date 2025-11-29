package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String vatNumber) throws UsernameNotFoundException {
        User user = userRepository.findByVatNumber(vatNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with VAT number: " + vatNumber));

        return new org.springframework.security.core.userdetails.User(
                user.getVatNumber(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    public User registerUser(String name, String surname, String companyName, String vatNumber,
                             String email, String address, String phoneNumber, String password) {
        if (userRepository.existsByVatNumber(vatNumber)) {
            throw new RuntimeException("VAT number already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setCompanyName(companyName);
        user.setVatNumber(vatNumber.toUpperCase());
        user.setEmail(email);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public User findByVatNumber(String vatNumber) {
        return userRepository.findByVatNumber(vatNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUser(Long userId, String name, String surname, String companyName,
                           String vatNumber, String email, String address, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if VAT number is being changed and if it's already taken by another user
        if (!user.getVatNumber().equals(vatNumber.toUpperCase())) {
            if (userRepository.existsByVatNumber(vatNumber.toUpperCase())) {
                throw new RuntimeException("VAT number already exists");
            }
        }

        // Check if email is being changed and if it's already taken by another user
        if (!user.getEmail().equals(email)) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already exists");
            }
        }

        user.setName(name);
        user.setSurname(surname);
        user.setCompanyName(companyName);
        user.setVatNumber(vatNumber.toUpperCase());
        user.setEmail(email);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
