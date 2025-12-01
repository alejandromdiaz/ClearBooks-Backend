package com.alemediaz.clearbooksbackend.dto;

public class LoginRequest {
    private String vatNumber;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String vatNumber, String password) {
        this.vatNumber = vatNumber;
        this.password = password;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
