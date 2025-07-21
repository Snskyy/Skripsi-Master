package com.inventory.inventory.controller;

import com.inventory.inventory.dto.transaction.CheckoutResponse;
import com.inventory.inventory.dto.transaction.TransactionRequest;
import com.inventory.inventory.dto.transaction.TransactionResponse;
import com.inventory.inventory.dto.transaction.TransactionStatusRequest;
import com.inventory.inventory.exception.BadRequestException;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ShopService;
import com.inventory.inventory.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ShopService shopService;

    @PostMapping
    public ResponseEntity<?> checkoutCart(
            @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        CheckoutResponse response = transactionService.checkoutCart(request, user.getId(), shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<TransactionResponse> transactions = transactionService.getTransactions(shopId, status);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTransactionStatus(
            @PathVariable Long id,
            @RequestBody TransactionStatusRequest body,
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        if (body.getStatus() == null || body.getStatus().isBlank()) {
            throw new BadRequestException("Status tidak boleh kosong.");
        }
        String updatedStatus = transactionService.updateTransactionStatus(
                id,
                body.getStatus().toUpperCase(),
                body.getNote(),
                user.getId()
        );
        // Kembalikan status baru ke frontend
        Map<String, String> response = new HashMap<>();
        response.put("status", updatedStatus);

        return ResponseEntity.ok(response);
    }

}
