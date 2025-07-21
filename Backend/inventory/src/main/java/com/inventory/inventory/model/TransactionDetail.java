package com.inventory.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transaction_details")
public class TransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;    // Relasi ke header
    private Long productId;
    private String productName; // ⬅️ Tambahan (disimpan snapshotnya)
    private Long variantId;        // nullable jika tidak ada varian
    private String sku; // ⬅️ Tambahan
    private String variantAttributes; // ⬅️ Tambahan (disimpan snapshotnya)
    private Long quantity;
    private Long price;
    private Long subTotal;
    private String flag; // status stocked agar tahu bahwa sudah di stock utnuk prodcuct ini
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate; // untuk soft delete
}
