package com.inventory.inventory.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transaction_headers")
public class TransactionHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;                // ID user yang melakukan transaksi (kasir)
    private String createBy;
    private Long shopId;                // Toko yang melakukan transaksi
    private Long platformId;            // Platform ID (Shopee, Tokopedia, dsb)
    private String platformName;        // Snapshot nama platform saat transaksi

    private String type;                // PURCHASE or SALES
    private Long supplierCustomerId;    // ID dari supplier/customer
    private String supplierCustomerName;// Snapshot nama supplier/customer

    private String paymentMethod;       // cash, transfer, e-wallet, etc
    private String note;                // Catatan tambahan

    private Long total;                 // Total keseluruhan
    private String status;              // PENDING, PAID, COMPLETED, dsb
    private String invoiceNumber; // ⬅️ Tambahan
    private LocalDateTime createDate;       // Tanggal dibuat
    private LocalDateTime updateDate;   // Terakhir diperbarui
    private LocalDateTime deleteDate;   // Jika dihapus (soft delete)
}
