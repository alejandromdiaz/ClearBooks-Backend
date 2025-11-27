package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
        user.setVatNumber(vatNumber.toUpperCase()); // Store VAT in uppercase
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
}
