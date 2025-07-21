package com.inventory.inventory.dto.category;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private int totalProduct;
}

