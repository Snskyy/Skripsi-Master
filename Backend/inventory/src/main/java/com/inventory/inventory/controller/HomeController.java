package com.inventory.inventory.controller;

import com.inventory.inventory.dto.shop.ShopUserNameResponse;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ShopService;
import com.inventory.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/homepage")
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @GetMapping("/shop-user")
    public ResponseEntity<?> getHomeData(@AuthenticationPrincipal User user, @RequestHeader("shop-id") Long shopId) {
        try {
            shopService.shopValidationWithUserId(user.getId(), shopId);

            return ResponseEntity.ok(shopService.getShopUserNameResponse(user.getName(), shopId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
