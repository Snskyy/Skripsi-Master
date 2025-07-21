package com.inventory.inventory.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "shops")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // ← simpan user ID manual tanpa @ManyToOne

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;

    @Column(nullable = false)
    private String email;
    private String shopUrl;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    // Tidak ada field User

}
