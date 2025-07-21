package com.inventory.inventory.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ABCResponse {
    private Long productId;
    private Long variantId;
    private String productName;
    private Long totalValue;
    private String category;
}
