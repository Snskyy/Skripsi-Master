package com.inventory.inventory.dto.transaction;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TransactionDetailResponse {
    private Long id;
    private Long transactionId;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantAttributes;
    private String SKU;
    private Long quantity;
    private Long price;
    private Long subTotal;
    private String flag;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
}
