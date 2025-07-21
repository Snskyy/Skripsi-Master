package com.inventory.inventory.dto.product;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private String sku;
    private String attributes; // "Size:M,Color:Red"
    private Long minimumStock;
    private Long price;
    private Long stock;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
//    private LocalDate updateDate;
}
