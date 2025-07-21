package com.inventory.inventory.dto.stock;

import lombok.Data;

@Data
public class StockRecommendationResponse {
    private Long stockId;
    private Long productId;
    private Long variantId;
    private String productName;
    private String sku;
    private String attributes;
    private Long minimumStock; // Saat ini
    private Long recommendedMinimumStock; // Rekomendasi baru
    private String recommendationReason;
    private String abcCategory;
}

