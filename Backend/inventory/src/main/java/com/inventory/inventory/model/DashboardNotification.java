package com.inventory.inventory.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "dashboard_notifications")
public class DashboardNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long shopId;

    private String type;     // e.g. "STOCK_RECOMMENDATION", "TRANSACTION_PENDING"
    private String key;      // identifier unik jika dibutuhkan untuk pengelompokan (misal: productId / invoiceNumber)
    private String title;
    private String content;

    private Boolean read = false;
    private Boolean resolved = false;  // ✅ Diisi true jika sudah ditindaklanjuti (bukan hanya dibaca)

    private LocalDateTime createDate;
    private LocalDateTime expireDate;
}
