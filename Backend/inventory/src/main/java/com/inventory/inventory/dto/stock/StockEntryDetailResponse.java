package com.inventory.inventory.dto.stock;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockEntryDetailResponse {
    private Long stockId;
    private Long productId;
    private String productName;
    private Long variantId;
    private String sku; // for variant
    private String attributes; // nicely formatted attributes (example: "Size:L, Color:Red")
    private Long quantity;
    private Long productQuantity;
    private Long variantQuantity;
    private Long minimumStock;
    private Long usedQuantity;
    private Long disabled;
    private String location;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

