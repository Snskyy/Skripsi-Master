package com.inventory.inventory.controller;

import com.inventory.inventory.dto.auth.ChangePasswordRequest;
import com.inventory.inventory.dto.user.UserResponse;
import com.inventory.inventory.dto.user.UserUpdateRequest;
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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @GetMapping("")
    public ResponseEntity<?> getUser(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        try {
            shopService.shopValidationWithUserId(user.getId(), shopId);
            return ResponseEntity.ok(userService.getUser(user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("")
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId,

            @RequestBody UserUpdateRequest request) {
        try {
            shopService.shopValidationWithUserId(user.getId(), shopId);
            UserResponse updatedUser = userService.updateUser(request, user.getId());
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            String message = userService.changePassword(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }



}
