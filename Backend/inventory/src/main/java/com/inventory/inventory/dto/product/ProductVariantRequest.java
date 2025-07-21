package com.inventory.inventory.dto.product;

import com.inventory.inventory.dto.stock.StockEntryRequest;
import lombok.Data;

import java.util.List;

@Data
public class ProductVariantRequest {
    private String id;
    private String productId;
    private String sku;
    private String attributes;
    private Long price;
    private Long minimumStock;
    private List<StockEntryRequest> stockEntries;
}
