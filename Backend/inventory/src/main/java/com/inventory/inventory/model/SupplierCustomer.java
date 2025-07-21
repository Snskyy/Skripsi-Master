package com.inventory.inventory.model;

import com.inventory.inventory.repository.ShopRepository;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "supplier_customer")
public class SupplierCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long shopId;
    private String name;
    private String type;
    private String phone;
    private String email;
    private String note;
    private Boolean isDefault = Boolean.FALSE;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
    private LocalDateTime lastTransactionDate;
}
