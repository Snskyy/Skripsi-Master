package com.inventory.inventory.dto.product;

import lombok.Data;

@Data
public class MinimumStockRequest {
    private Long productId;
    private Long variantId; // null jika tidak pakai varian
    private Long minimumStock;
}
