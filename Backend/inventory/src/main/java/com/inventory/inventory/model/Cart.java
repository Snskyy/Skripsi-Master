package com.inventory.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long shopId;
    private Long productId;
    private Long variantId;
    private Long quantity;
    private Long price;
    private String type;
    private LocalDateTime createDate;
}
