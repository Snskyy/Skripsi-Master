package com.inventory.inventory.controller;

import com.inventory.inventory.dto.product.ProductVariantRequest;
import com.inventory.inventory.dto.product.ProductVariantResponse;
import com.inventory.inventory.model.ProductVariant;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ProductVariantService;
import com.inventory.inventory.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
public class ProductVariantController {
    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ShopService shopService;

    @PostMapping("/variants")
    public ResponseEntity<ProductVariantResponse> createVariant(
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user,
            @RequestBody ProductVariantRequest request) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        ProductVariantResponse response = productVariantService.createProductVariant(shopId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/variants/{variantId}/update")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user,
            @PathVariable Long variantId,
            @RequestBody ProductVariantRequest request) {
        // Validasi user memiliki akses ke shop
        shopService.shopValidationWithUserId(user.getId(), shopId);
        ProductVariantResponse response = productVariantService.updateProductVariant(shopId, variantId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/variants/{variantId}/delete")
    public ResponseEntity<Void> softDeleteVariant(
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user,
            @PathVariable Long variantId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        productVariantService.softDeleteVariant(variantId, shopId, user.getId());
        return ResponseEntity.noContent().build(); // 204
    }

}
