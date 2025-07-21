package com.inventory.inventory.repository;

import com.inventory.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository  extends JpaRepository<Product, Long> {
    List<Product> findByShopIdAndCategoryIdIsNullAndDeleteDateIsNull(Long shopId);
    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId AND p.deleteDate IS NULL")
    int countActiveByCategoryId(@Param("categoryId") Long categoryId);
    @Query("SELECT p FROM Product p WHERE p.shopId = :shopId AND p.deleteDate IS NULL")
    List<Product> findActiveByShopId(@Param("shopId") Long shopId);
    List<Product> findByCategoryId(Long categoryId);
    Optional<Product> findByIdAndShopId(Long id, Long shopId);
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shopId = :shopId AND p.categoryId IS NULL AND p.deleteDate IS NULL")
    int countByShopIdAndCategoryIdIsNullAndDeleteDateIsNull(@Param("shopId") Long shopId);
}
