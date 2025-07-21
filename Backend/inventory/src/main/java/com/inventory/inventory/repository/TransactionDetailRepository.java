package com.inventory.inventory.repository;


import com.inventory.inventory.model.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
    List<TransactionDetail> findByTransactionId(Long id);

    @Query("""
    SELECT AVG(td.quantity)
    FROM TransactionDetail td
    JOIN TransactionHeader th ON td.transactionId = th.id
    WHERE td.productId = :productId
      AND (:variantId IS NULL OR td.variantId = :variantId)
      AND th.shopId = :shopId
      AND th.type = 'SALES'
      AND th.deleteDate IS NULL
      AND th.status NOT IN ('CANCELED', 'PENDING')
      AND td.createDate BETWEEN :start AND :end
""")
    Double avgSalesBetweenValidStatus(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("shopId") Long shopId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT SUM(td.quantity)
    FROM TransactionDetail td
    JOIN TransactionHeader th ON td.transactionId = th.id
    WHERE td.productId = :productId
      AND (:variantId IS NULL OR td.variantId = :variantId)
      AND th.shopId = :shopId
      AND th.type = 'SALES'
      AND th.deleteDate IS NULL
      AND th.status NOT IN ('CANCELED', 'PENDING')
      AND td.createDate >= :startDate
""")
    Long sumSalesBetweenValidStatus(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate
    );
    @Query("""
SELECT COUNT(DISTINCT DATE(td.createDate))
FROM TransactionDetail td
JOIN TransactionHeader th ON td.transactionId = th.id
WHERE td.productId = :productId
  AND (:variantId IS NULL OR td.variantId = :variantId)
  AND th.shopId = :shopId
  AND th.type = 'SALES'
  AND th.status NOT IN ('CANCELED', 'PENDING')
  AND th.deleteDate IS NULL
  AND td.createDate >= :startDate
""")
    int countActiveSaleDays(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate
    );


    @Query(value = """
    SELECT td.product_id AS productId, SUM(td.quantity) AS totalQuantity
    FROM transaction_details td
    JOIN transaction_headers th ON td.transaction_id = th.id
    JOIN products p ON td.product_id = p.id
    WHERE th.shop_id = :shopId
      AND th.type = 'SALES'
      AND th.status = 'FINAL'
      AND p.delete_date IS NULL
      AND p.shop_id = :shopId
    GROUP BY td.product_id
    ORDER BY totalQuantity DESC
    LIMIT 10
""", nativeQuery = true)
    List<Object[]> findTopSellingProductsFiltered(@Param("shopId") Long shopId);

    @Query("""
    SELECT td
    FROM TransactionDetail td
    JOIN TransactionHeader th ON td.transactionId = th.id
    WHERE th.shopId = :shopId
      AND th.type = 'SALES'
      AND th.status = 'FINAL'
      AND th.deleteDate IS NULL
      AND td.deleteDate IS NULL
""")
    List<TransactionDetail> findSalesByShopId(@Param("shopId") Long shopId);


}