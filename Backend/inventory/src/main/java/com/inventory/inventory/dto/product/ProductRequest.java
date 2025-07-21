package com.inventory.inventory.dto.product;

import com.inventory.inventory.dto.stock.StockEntryRequest;
import lombok.Data;

import java.util.List;
@Data
    public class ProductRequest {
        private Long shopId;
        private Long categoryId;
        private String name;
        private String description;
        private Long minimumStock;
        private Boolean hasVariant;
        private Long price; // only used if hasVariant is false

        private Boolean discountStatus;
        private Long discountValue;

        private List<StockEntryRequest> stockEntries; // used if hasVariant is false
        private List<ProductVariantRequest> variants; // used if hasVariant is true
    }
