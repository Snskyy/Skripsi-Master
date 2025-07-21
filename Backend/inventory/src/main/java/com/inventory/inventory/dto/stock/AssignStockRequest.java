package com.inventory.inventory.dto.stock;

import lombok.Data;

import java.util.List;

@Data
public class AssignStockRequest {
    private Long transactionDetailId;
    private Long productId;
    private Long variantId;
    private String note;
    private List<LocationQuantity> locations;
    @Data
    public static class LocationQuantity {
        private String location;
        private Long quantity;
    }

}
