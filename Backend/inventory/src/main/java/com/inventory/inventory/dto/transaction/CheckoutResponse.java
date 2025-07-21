package com.inventory.inventory.dto.transaction;

import lombok.Data;

@Data
public class CheckoutResponse {
    private String invoiceNumber;
    private Long supplierCustomerId;
    private String supplierCustomerName;
}
