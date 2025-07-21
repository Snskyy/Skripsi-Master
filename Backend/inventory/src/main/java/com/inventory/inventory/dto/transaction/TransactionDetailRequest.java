package com.inventory.inventory.dto.transaction;

import lombok.Data;

@Data
public class TransactionDetailRequest {
    private Long productId;
    private Long variantId; // nullable jika tidak ada varian
    private Long quantity;
    private Long price;
}
