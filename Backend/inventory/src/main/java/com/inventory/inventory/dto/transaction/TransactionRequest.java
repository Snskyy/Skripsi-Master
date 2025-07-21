package com.inventory.inventory.dto.transaction;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionRequest {
    private Long userId;
    private String createBy;
    private Long shopId;
    private Long platformId; // opsional untuk integrasi toko online
    private Long platformName;
    private String type; // "PURCHASE" atau "SALES"
    private String supplierCustomerName;
    private Long supplierCustomerId; // untuk relasi ke tabel supplier-customer jika ada
    private String note;
    private Long total;
    private String status; // PENDING, PAID, dll.
    private String paymentMethod; // cash, transfer, dll.
    private String InvoiceNumber;

    private List<TransactionDetailRequest> items;
}