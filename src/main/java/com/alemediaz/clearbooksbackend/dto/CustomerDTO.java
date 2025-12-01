package com.alemediaz.clearbooksbackend.dto;

public class CustomerDTO {
    private Long id;
    private String name;
    private String vatNumber;
    private String phone;
    private String address;

    public CustomerDTO() {
    }

    public CustomerDTO(Long id, String name, String vatNumber, String phone, String address) {
        this.id = id;
        this.name = name;
        this.vatNumber = vatNumber;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
