package com.inventory.inventory.repository;

import com.inventory.inventory.model.Category;
import com.inventory.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByShopIdAndDeleteDateIsNull(Long shopId);

}
