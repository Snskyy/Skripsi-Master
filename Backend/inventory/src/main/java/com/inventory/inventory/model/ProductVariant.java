package com.inventory.inventory.model;
import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId; // foreign key manually set

    private String sku;
    private String attributes; // e.g. "Size:M,Color:Red"
    private Long minimumStock;
    private Long stock;
    private Long price;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
}
