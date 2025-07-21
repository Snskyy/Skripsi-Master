package com.inventory.inventory.dto.product;

import lombok.Data;
@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Long categoryId;
    private Long minimumStock;
    private Long price;
}
