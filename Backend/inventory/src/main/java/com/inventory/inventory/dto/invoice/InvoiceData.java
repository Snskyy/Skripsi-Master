package com.inventory.inventory.dto.invoice;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceData {
    private String invoiceNumber;
    private LocalDateTime date;
    private String supplierCustomerName;
    private String cashierName;
    private String paymentMethod;
    private String status;

    private List<InvoiceItem> items;
    private Long total;
}