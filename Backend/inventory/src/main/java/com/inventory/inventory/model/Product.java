package com.inventory.inventory.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long shopId;
    private Long categoryId;
    private String name;
    private String description;

    private Boolean hasVariant;
    private Long price;
    private Long stock;
    private Long minimumStock;
    private Boolean discountStatus;
    private Long discountValue;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
}
