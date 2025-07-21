package com.inventory.inventory.dto.product;

import com.inventory.inventory.dto.stock.StockSummaryResponse;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetailResponse {
    private ProductResponse product;

    // Only used if product has variants
    private List<ProductVariantResponse> variants;

    // Optional stock summary
    private List<StockSummaryResponse> stockEntries;
    private Long totalStock;
    private String thumbnailUrl;
}
