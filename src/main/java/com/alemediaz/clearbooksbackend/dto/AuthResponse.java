package com.alemediaz.clearbooksbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String surname;
    private String companyName;
    private String vatNumber;
    private String email;
    private String address;
    private String phoneNumber;
}
