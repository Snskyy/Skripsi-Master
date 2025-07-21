package com.inventory.inventory.dto.product;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String categoryName;
    private String name;
    private String description;
    private Long totalStock;
    private Boolean hasVariant;
    private Long stock;
    private Long price;
    private Long minimumStock;
    private Boolean discountStatus;
    private Long discountValue;
    private String thumbnailUrl;
    private LocalDateTime updateDate;
    private LocalDateTime createDate;
}
