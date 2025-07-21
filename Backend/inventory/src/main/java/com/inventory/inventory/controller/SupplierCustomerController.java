package com.inventory.inventory.controller;

import com.inventory.inventory.dto.suppliercustomer.SupplierCustomerRequest;
import com.inventory.inventory.dto.suppliercustomer.SupplierCustomerResponse;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ShopService;
import com.inventory.inventory.service.SupplierCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-customer")
public class SupplierCustomerController {

    @Autowired
    private SupplierCustomerService supplierCustomerService;
    @Autowired
    private ShopService shopService;

    @GetMapping
    public ResponseEntity<List<SupplierCustomerResponse>> getByTypeAndShop(
            @AuthenticationPrincipal User user,
            @RequestParam String type,
            @RequestHeader("shop-id") Long shopId) {

        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<SupplierCustomerResponse> list = supplierCustomerService.getByTypeAndShop(type, shopId);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<SupplierCustomerResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody SupplierCustomerRequest request,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        SupplierCustomerResponse response = supplierCustomerService.save(shopId,request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<SupplierCustomerResponse> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestHeader("shop-id") Long shopId,
            @RequestBody SupplierCustomerRequest request) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        SupplierCustomerResponse response = supplierCustomerService.update(id,request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        supplierCustomerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
