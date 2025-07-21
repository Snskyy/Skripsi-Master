package com.inventory.inventory.controller;

import com.inventory.inventory.dto.product.MinimumStockRequest;
import com.inventory.inventory.dto.product.MinimumStockResponse;
import com.inventory.inventory.dto.stock.*;
import com.inventory.inventory.model.Stock;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ShopService;
import com.inventory.inventory.service.StockHistoryService;
import com.inventory.inventory.service.StockRecommendationService;
import com.inventory.inventory.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockHistoryService stockHistoryService;

    @Autowired
    private StockRecommendationService stockRecommendationService;

    @Autowired
    private ShopService shopService;

    @PostMapping
    public ResponseEntity<StockEntryDetailResponse> createStock(
            @AuthenticationPrincipal User user,
            @RequestBody StockEntryRequest request,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        if (request.getProductId() == null && request.getVariantId() == null) {
            return ResponseEntity.badRequest().build();
        }

        StockEntryDetailResponse response = stockService.createStockEntry(request, user.getId(), shopId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Stock>> getStockByProductId(
            @PathVariable Long productId) {
        List<Stock> stocks = stockService.getStockByProductId(productId);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<Stock>> getStockByVariantId(
            @PathVariable Long variantId) {
        List<Stock> stocks = stockService.getStockByVariantId(variantId);
        return ResponseEntity.ok(stocks);
    }

    @PutMapping("/{stockId}/update")
    public ResponseEntity<StockEntryDetailResponse> updateStock(
            @AuthenticationPrincipal User user,
            @PathVariable Long stockId,
            @RequestBody StockEntryRequest request,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        StockEntryDetailResponse updatedStock = stockService.updateStock(user.getId(), stockId, request);
        return ResponseEntity.ok(updatedStock);
    }

    @DeleteMapping("/{stockId}/delete")
    public ResponseEntity<Void> deleteStock(
            @AuthenticationPrincipal User user,
            @PathVariable Long stockId,
            @RequestHeader("shop-id") Long shopId,
            @RequestBody Map<String, String> payload) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        String note = payload.get("note");
        if(note == null){
            note = "";
        }
        stockService.softDeleteStockById(user.getId(), stockId, note);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<StockEntryDetailResponse> getAllStocks(
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        return stockService.getAllStockDetails(shopId);
    }

    @GetMapping("/{stockId}/logs")
    public ResponseEntity<List<StockHistoryResponse>> getStockLogs(
            @PathVariable Long stockId,
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<StockHistoryResponse> logs = stockHistoryService.getStockLogsByStockId(stockId);
        return ResponseEntity.ok(logs);

    }

    @PostMapping("assign")
    public ResponseEntity<List<AssignStockResponse>> assignStock(
            @AuthenticationPrincipal User user,
            @RequestBody AssignStockRequest request,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<AssignStockResponse> responses = stockService.assignStock(request, user.getId(), shopId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<StockRecommendationResponse>> getStockRecommendations(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        List<StockRecommendationResponse> recs = stockRecommendationService.generateStockRecommendations(user.getId(), shopId);
        return ResponseEntity.ok(recs);
    }

    @PatchMapping("/minimum-stock")
    public ResponseEntity<MinimumStockResponse> updateMinimumStockByProduct(
            @RequestBody MinimumStockRequest request,
            @RequestHeader("shop-id") Long shopId) {

        MinimumStockResponse response = stockService.updateMinimumStockByProduct(
                request.getProductId(),
                request.getVariantId(),
                request.getMinimumStock()
        );
        return ResponseEntity.ok(response);
    }
}
