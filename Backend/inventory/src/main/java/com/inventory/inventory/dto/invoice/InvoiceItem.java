package com.inventory.inventory.dto.invoice;

import lombok.Data;

@Data
public class InvoiceItem {
    private String productName;
    private String variantName;
    private Long quantity;
    private Long price;
    private Long subtotal;
}