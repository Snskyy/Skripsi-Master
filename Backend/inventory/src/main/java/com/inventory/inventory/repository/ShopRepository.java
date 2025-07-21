package com.inventory.inventory.repository;

import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByUserId(Long userId);
    Optional<Object> findByName(String shopName);
}
