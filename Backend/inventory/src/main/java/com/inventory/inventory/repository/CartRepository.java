package com.inventory.inventory.repository;

import com.inventory.inventory.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserIdAndShopIdAndType(Long userId, Long shopId, String Type);
    Optional<Cart> findByUserIdAndShopIdAndProductIdAndVariantIdAndType(
            Long userId,
            Long shopId,
            Long productId,
            Long variantId,
            String type
    );
    void deleteByUserIdAndShopIdAndType(Long userId, Long shopId, String type);

    @Query("""
    SELECT c FROM Cart c
    WHERE c.shopId = :shopId
      AND c.type = :type
      AND c.productId = :productId
      AND (:variantId IS NULL OR c.variantId = :variantId)
      AND c.id <> :excludedCartId
""")
    List<Cart> findSimilarCartsExcludingId(
            @Param("shopId") Long shopId,
            @Param("type") String type,
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("excludedCartId") Long excludedCartId
    );

}
