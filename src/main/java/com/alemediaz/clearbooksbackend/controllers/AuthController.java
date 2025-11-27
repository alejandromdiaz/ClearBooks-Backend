package com.alemediaz.clearbooksbackend.controllers;

import com.alemediaz.clearbooksbackend.dto.AuthResponse;
import com.alemediaz.clearbooksbackend.dto.LoginRequest;
import com.alemediaz.clearbooksbackend.dto.RegisterRequest;
import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.security.JWTUtil;
import com.alemediaz.clearbooksbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                    request.getName(),
                    request.getSurname(),
                    request.getCompanyName(),
                    request.getVatNumber(),
                    request.getEmail(),
                    request.getAddress(),
                    request.getPhoneNumber(),
                    request.getPassword()
            );

            String token = jwtUtil.generateToken(user.getVatNumber());

            return ResponseEntity.ok(
                    new AuthResponse(token, user.getName(), user.getSurname(),
                            user.getCompanyName(), user.getVatNumber(), user.getEmail(),
                            user.getAddress(), user.getPhoneNumber())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getVatNumber().toUpperCase(),
                            request.getPassword()
                    )
            );

            User user = userService.findByVatNumber(request.getVatNumber().toUpperCase());
            String token = jwtUtil.generateToken(user.getVatNumber());

            return ResponseEntity.ok(
                    new AuthResponse(token, user.getName(), user.getSurname(),
                            user.getCompanyName(), user.getVatNumber(), user.getEmail(),
                            user.getAddress(), user.getPhoneNumber())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }
}
