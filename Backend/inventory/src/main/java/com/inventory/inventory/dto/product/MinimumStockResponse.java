package com.inventory.inventory.dto.product;

import lombok.Data;

@Data
public class MinimumStockResponse {
    private Long productId;
    private Long variantId;
    private Long newMinimumStock;
}
