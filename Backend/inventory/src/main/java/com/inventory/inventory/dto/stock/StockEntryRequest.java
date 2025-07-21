package com.inventory.inventory.dto.stock;

import lombok.Data;

@Data
public class StockEntryRequest {
    private Long id;          // Nullable (if null, means it's a new stock)
    private Long productId;
    private Long variantId;
    private Long quantity;
//    private String variantName;
    private String note;
    private String location;
    private Long disable;
}
