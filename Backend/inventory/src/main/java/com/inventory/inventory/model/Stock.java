    package com.inventory.inventory.model;


    import jakarta.persistence.*;
    import lombok.Data;

    import java.time.LocalDate;
    import java.time.LocalDateTime;

    @Entity
    @Data
    @Table(name = "stocks")
    public class Stock {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private Long shopId;

        // One of these two will be filled, depending on the product type
        private Long productId;       // for products without variants
        private Long variantId;       // for variant-based stock

        private Long quantity;
        private Long usedQuantity;    //how much of this entry has been used
        private String location;      // optional: warehouse, store, etc.
        private Long disabled;
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
        private LocalDateTime deleteDate;

    }
