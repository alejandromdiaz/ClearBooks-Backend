package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String vatNumber;
    private String password;
}
