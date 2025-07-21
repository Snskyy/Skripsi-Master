package com.inventory.inventory.dto.product;

import lombok.Data;

@Data
public class ProductCategoryRequest {
    private Long categoryId; // boleh null jika ingin menghapus kategori
}
