package com.inventory.inventory.service;

import com.inventory.inventory.dto.stock.StockHistoryResponse;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.StockHistory;
import com.inventory.inventory.repository.ShopRepository;
import com.inventory.inventory.repository.StockHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockHistoryService {
    @Autowired
    private StockHistoryRepository stockHistoryRepository;
//    @Autowired
//    private ShopRepository shopRepository;

    public List<StockHistoryResponse> getStockLogsByStockId(Long stockId) {
//        Shop shop = shopRepository.findById(shopId)
//                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));
//
//        if (!shop.getUserId().equals(userId)) {
//            throw new UserUnauthorizedException("Access Denied: You are not authorize.");
//        }

        List<StockHistory> stockHistories = stockHistoryRepository.findByStockId(stockId);
        return stockHistories.stream()
                .map(this::convertToStockHistoryResponse)
                .collect(Collectors.toList());
    }

    // Convert to StockHistoryResponse
    private StockHistoryResponse convertToStockHistoryResponse(StockHistory stockHistory) {
        StockHistoryResponse response = new StockHistoryResponse();
        response.setId(stockHistory.getId());
        response.setProductId(stockHistory.getProductId());
        response.setVariantId(stockHistory.getVariantId());
        response.setStockId(stockHistory.getStockId());
        response.setStockInOut(stockHistory.getStockInOut());
        response.setStockType(stockHistory.getStockType());
        response.setQuantity(stockHistory.getQuantity());
        response.setLocation(stockHistory.getLocation());
        response.setCreateDate(stockHistory.getCreateDate());
        response.setCreatedBy(stockHistory.getCreatedBy());
        response.setTransactionDetailId(stockHistory.getTransactionDetailId());
        response.setNote(stockHistory.getNote());
        return response;
    }
}
