package com.inventory.inventory.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "stocks_history")
public class StockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long shopId;


    // Link to product
    private Long productId;

    // Link to variant (nullable if the product has no variants)
    private Long variantId;

    // Link to stock entry (optional, for precise tracking of warehouse/location)
    private Long stockId;

    // Type of operation: "IN", "OUT", "ADJUST"
    private String stockInOut;

    // Reason for stock change: "PURCHASE", "SALE", "RETURN", "MANUAL_ADJUSTMENT", etc.
    private String stockType;

    private Long quantity;

    private String location; // warehouse, store, etc. (from Stock if needed)

    private LocalDateTime createDate;

    // Optional: userId for audit (who performed this action)
    private Long createdBy;

    // ✅ ID dari TransactionDetail jika berasal dari transaksi (untuk rollback akurat)
    private Long transactionDetailId;

    private String note;
}
