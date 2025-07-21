package com.inventory.inventory.controller;

import com.inventory.inventory.dto.category.CategoryRequest;
import com.inventory.inventory.dto.category.CategoryResponse;
import com.inventory.inventory.model.Category;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.CategoryService;
import com.inventory.inventory.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ShopService shopService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<CategoryResponse> categories = categoryService.getAllCategoryByShopId(shopId);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<?> addCategory(
            @RequestBody CategoryRequest request,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        Category saved = categoryService.createCategory(request, shopId);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        Category updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<Product> removedProducts = categoryService.deleteCategory(id);
        List<Map<String, Object>> removedProductMapList = removedProducts.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("name", p.getName());
                    return map;
                })
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Kategori berhasil dihapus");
        response.put("removedProducts", removedProductMapList);

        return ResponseEntity.ok(response);
    }
}

