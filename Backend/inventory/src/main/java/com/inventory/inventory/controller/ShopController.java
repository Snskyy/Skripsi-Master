package com.inventory.inventory.controller;

import com.inventory.inventory.dto.shop.ShopResponse;
import com.inventory.inventory.dto.shop.ShopUpdateRequest;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
public class ShopController {
    @Autowired
    ShopService shopService;

    @GetMapping
    public ResponseEntity<?> getShop(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId ){
        try {
            return ResponseEntity.ok(shopService.getShop(shopId, user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateShop(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId,
            @RequestBody ShopUpdateRequest request) {
        try {
            ShopResponse updatedUser = shopService.updateShop(shopId, request, user.getId());
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserShops(
            @AuthenticationPrincipal User user) {
        List<ShopResponse> responses = shopService.getShopsByUserId(user);
        return ResponseEntity.ok(responses);
    }

}
