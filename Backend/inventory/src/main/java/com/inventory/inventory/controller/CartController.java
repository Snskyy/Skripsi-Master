package com.inventory.inventory.controller;

import com.inventory.inventory.dto.cart.CartRequest;
import com.inventory.inventory.dto.cart.CartResponse;
import com.inventory.inventory.dto.cart.ListCartResponse;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.CartService;
import com.inventory.inventory.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private ShopService shopService;

//    @PostMapping("/bulk")
//    public ResponseEntity<List<CartResponse>> createBulkCart(
//            @RequestBody List<CartRequest> requests,
//            @AuthenticationPrincipal User user,
//            @RequestHeader("shop-id") Long shopId) {
//        shopService.shopValidationWithUserId(user.getId(), shopId);
//        List<CartResponse> responses = cartService.createCarts(requests, user.getId(), shopId);
//        return ResponseEntity.ok(responses);
//    }


    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
            @RequestBody CartRequest request,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        CartResponse response = cartService.createCart(request, user.getId(), shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ListCartResponse> getCart(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId,
            @RequestParam String type // e.g. "purchase"
    ) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        ListCartResponse response = cartService.getCartByUserShopType(user.getId(), shopId, type.toUpperCase());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<CartResponse> deleteCart(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        CartResponse deleted = cartService.deleteCartAndReturn(id);
        return ResponseEntity.ok(deleted);
    }

    @PutMapping("/{cartId}/update")
    public ResponseEntity<Void> updateCart(
            @PathVariable Long cartId,
            @RequestBody CartRequest request,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        cartService.updateCartPurchase(cartId, request, shopId);
        return ResponseEntity.ok().build(); // return status 200 OK tanpa body
    }
    @DeleteMapping("/all")
    public ResponseEntity<?> clearCart(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId,
            @RequestParam String type
    ) {
        try {
            shopService.shopValidationWithUserId(user.getId(), shopId);
//            System.out.println(">>> [Controller] clearCart called with userId=" + user.getId() + ", shopId=" + shopId + ", type=" + type);
            cartService.clearCart(user.getId(), shopId, type.toUpperCase());

            return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
        } catch (Exception e) {
//            System.out.println("!!! [Controller] Exception saat clearCart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal mengosongkan keranjang", "detail", e.getMessage()));
        }
    }

}
