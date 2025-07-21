package com.inventory.inventory.dto.suppliercustomer;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SupplierCustomerResponse {
    private Long id;
    private String name;
    private String type;
    private String phone;
    private String email;
    private String note;
    private Boolean isDefault;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime lastTransactionDate;
}