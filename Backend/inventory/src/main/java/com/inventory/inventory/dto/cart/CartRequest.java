package com.inventory.inventory.dto.cart;

import lombok.Data;

@Data
public class CartRequest {
    private Long id;
    private Long productId;
    private Long variantId;
    private Long quantity;
    private Long price;
    private String type;
}

