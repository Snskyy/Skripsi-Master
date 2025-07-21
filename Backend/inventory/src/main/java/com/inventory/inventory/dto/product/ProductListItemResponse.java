package com.inventory.inventory.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductListItemResponse {
    private Long productId;
    private String name;
    private String categoryName;
    private String description;
    private Boolean hasVariant;
    private String thumbnailUrl;
    private Long totalStock;
    private Long minimumStock;

    private List<ProductVariantSummary> variants;
}
