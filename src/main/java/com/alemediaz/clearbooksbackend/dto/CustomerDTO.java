package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String vatNumber;
    private String phone;
    private String address;
}
