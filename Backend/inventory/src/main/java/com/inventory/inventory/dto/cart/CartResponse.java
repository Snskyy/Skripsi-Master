package com.inventory.inventory.dto.cart;

import lombok.Data;


@Data
public class CartResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;
    private String sku;
    private Long quantity;
    private Long price;
    private Long subTotal;

}