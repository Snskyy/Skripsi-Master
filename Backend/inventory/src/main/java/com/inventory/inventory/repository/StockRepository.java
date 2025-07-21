package com.inventory.inventory.repository;

import com.inventory.inventory.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s FROM Stock s WHERE s.productId = :productId AND s.deleteDate IS NULL AND s.disabled = 0")
    List<Stock> findActiveByProductId(@Param("productId") Long productId);

    @Query("SELECT s FROM Stock s WHERE s.variantId = :variantId AND s.deleteDate IS NULL AND s.disabled = 0")
    List<Stock> findActiveByVariantId(@Param("variantId") Long variantId);

    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.productId = :productId AND s.deleteDate IS NULL")
    Long sumQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.variantId = :variantId AND s.deleteDate IS NULL")
    Long sumQuantityByVariantId(@Param("variantId") Long variantId);
    List<Stock> findByProductIdOrderByCreateDateAsc(Long productId);
    List<Stock> findByVariantIdOrderByCreateDateAsc(Long variantId);
    @Query("SELECT SUM(s.quantity - COALESCE(s.usedQuantity, 0)) " +
            "FROM Stock s WHERE s.productId = :productId AND s.deleteDate IS NULL")
    Long sumAvailableQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(s.quantity - COALESCE(s.usedQuantity, 0)) " +
            "FROM Stock s WHERE s.variantId = :variantId AND s.deleteDate IS NULL")
    Long sumAvailableQuantityByVariantId(@Param("variantId") Long variantId);

    List<Stock> findAllByShopIdAndDeleteDateIsNull(Long shopId);

    @Query("SELECT s FROM Stock s WHERE s.productId = :productId AND s.disabled = :disabled")
    List<Stock> findByProductIdAndDisabled(@Param("productId") Long productId, @Param("disabled") Integer disabled);

    @Query("SELECT s FROM Stock s WHERE s.variantId = :variantId AND s.disabled = :disabled")
    List<Stock> findByVariantIdAndDisabled(@Param("variantId") Long variantId, @Param("disabled") Integer disabled);

    @Query("SELECT SUM(s.quantity - COALESCE(s.usedQuantity, 0)) " +
            "FROM Stock s WHERE s.variantId = :variantId AND s.deleteDate IS NULL AND s.disabled = 0")
    Long sumAvailableStockByVariantId(@Param("variantId") Long variantId);

    @Query("SELECT SUM(s.quantity - COALESCE(s.usedQuantity, 0)) " +
            "FROM Stock s WHERE s.productId = :productId AND s.variantId IS NULL AND s.deleteDate IS NULL AND s.disabled = 0")
    Long sumAvailableStockByProductId(@Param("productId") Long productId);
    @Query(value = """
SELECT 
    s.product_id,
    s.variant_id,
    COALESCE(v.minimum_stock, p.minimum_stock, 0) AS minimum_stock,
    COALESCE(SUM(s.quantity - COALESCE(s.used_quantity, 0)), 0) AS available_quantity
FROM stocks s
LEFT JOIN products p ON s.product_id = p.id
LEFT JOIN product_variants v ON s.variant_id = v.id
WHERE s.shop_id = :shopId
  AND s.disabled = 0
  AND s.delete_date IS NULL
  AND p.delete_date IS NULL
GROUP BY s.product_id, s.variant_id, v.minimum_stock, p.minimum_stock
""", nativeQuery = true)
    List<Object[]> fetchStockSummary(@Param("shopId") Long shopId);







}
