package com.alemediaz.clearbooksbackend.dto;

public class RegisterRequest {
    private String name;
    private String surname;
    private String companyName;
    private String vatNumber;
    private String email;
    private String address;
    private String phoneNumber;
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String surname, String companyName, String vatNumber,
                           String email, String address, String phoneNumber, String password) {
        this.name = name;
        this.surname = surname;
        this.companyName = companyName;
        this.vatNumber = vatNumber;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
