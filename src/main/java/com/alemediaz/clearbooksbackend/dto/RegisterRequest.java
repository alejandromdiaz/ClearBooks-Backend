package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String surname;
    private String companyName;
    private String vatNumber;
    private String email;
    private String address;
    private String phoneNumber;
    private String password;
}
