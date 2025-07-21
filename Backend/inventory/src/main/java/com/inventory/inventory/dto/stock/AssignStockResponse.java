package com.inventory.inventory.dto.stock;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignStockResponse {
    private Long transactionDetailId;
    private Long transactionId;
    private Long productId;
    private Long variantId;
    private String productName;
    private String sku;
    private String variantAttributes;
    private Boolean stocked;
    private Long productQuantity;
    private Long variantQuantity;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private Long disabled;


    private List<LocationEntry> locations; // Ganti dari LocationQuantity

    @Data
    public static class LocationEntry {
        private Long stockId;      // <--- Penting untuk edit/hapus stock
        private String location;
        private Long quantity;
    }
}