package com.inventory.inventory.dto.product;

import lombok.Data;

import java.util.Map;

@Data
public class ProductVariantSummary {
    private Long variantId;
    private String sku;
    private Long minimumStock;
    private Map<String, String> attributes; // e.g., {"Size":"M", "Color":"Red"}
    private Long stock;
}
