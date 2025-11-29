package com.alemediaz.clearbooksbackend.controllers;

import com.alemediaz.clearbooksbackend.dto.AuthResponse;
import com.alemediaz.clearbooksbackend.dto.ChangePasswordRequest;
import com.alemediaz.clearbooksbackend.dto.UpdateUserRequest;
import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.security.JWTUtil;
import com.alemediaz.clearbooksbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final JWTUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JWTUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());

            AuthResponse response = new AuthResponse(
                    null, // Don't send token in profile GET
                    user.getName(),
                    user.getSurname(),
                    user.getCompanyName(),
                    user.getVatNumber(),
                    user.getEmail(),
                    user.getAddress(),
                    user.getPhoneNumber()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching profile");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        try {
            User currentUser = userService.findByVatNumber(authentication.getName());

            User updatedUser = userService.updateUser(
                    currentUser.getId(),
                    request.getName(),
                    request.getSurname(),
                    request.getCompanyName(),
                    request.getVatNumber(),
                    request.getEmail(),
                    request.getAddress(),
                    request.getPhoneNumber()
            );

            // Generate new token with updated VAT number (in case it changed)
            String token = jwtUtil.generateToken(updatedUser.getVatNumber());

            AuthResponse response = new AuthResponse(
                    token,
                    updatedUser.getName(),
                    updatedUser.getSurname(),
                    updatedUser.getCompanyName(),
                    updatedUser.getVatNumber(),
                    updatedUser.getEmail(),
                    updatedUser.getAddress(),
                    updatedUser.getPhoneNumber()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());

            userService.changePassword(
                    user.getId(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok().body("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
