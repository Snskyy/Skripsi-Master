package com.inventory.inventory.dto.suppliercustomer;

import lombok.Data;

@Data
public class SupplierCustomerRequest {
    private Long id; // tambahkan ini
    private String name;
    private String type;
    private String phone;
    private String email;
    private String note;
    private Boolean isDefault; // Tambahkan ini
}
