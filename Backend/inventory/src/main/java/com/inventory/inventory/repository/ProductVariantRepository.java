package com.inventory.inventory.repository;

import com.inventory.inventory.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository  extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndDeleteDateIsNull(Long productId);

    @Query("SELECT COUNT(v) > 0 FROM ProductVariant v WHERE v.productId = :productId AND v.sku = :sku AND v.deleteDate IS NULL")
    boolean existsActiveByProductIdAndSku(@Param("productId") Long productId, @Param("sku") String sku);

}
