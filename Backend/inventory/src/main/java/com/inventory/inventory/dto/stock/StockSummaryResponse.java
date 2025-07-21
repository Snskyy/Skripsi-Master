package com.inventory.inventory.dto.stock;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockSummaryResponse {
    private Long id;

    private Long productId;  // used if no variants
    private Long variantId;// used if product has variants
    private String variantName;
    private Long quantity;
    private String location;
    private String sku;
    private Long usedQuantity;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
