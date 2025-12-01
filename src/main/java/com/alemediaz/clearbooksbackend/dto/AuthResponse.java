package com.alemediaz.clearbooksbackend.dto;

public class AuthResponse {
    private String token;
    private String name;
    private String surname;
    private String companyName;
    private String vatNumber;
    private String email;
    private String address;
    private String phoneNumber;

    public AuthResponse() {
    }

    public AuthResponse(String token, String name, String surname, String companyName,
                        String vatNumber, String email, String address, String phoneNumber) {
        this.token = token;
        this.name = name;
        this.surname = surname;
        this.companyName = companyName;
        this.vatNumber = vatNumber;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
