package com.inventory.inventory.service;

import com.inventory.inventory.dto.dashboard.ABCResponse;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.TransactionDetail;
import com.inventory.inventory.repository.ShopRepository;
import com.inventory.inventory.repository.TransactionDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisService {
    @Autowired
    private ShopService shopService;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    public List<ABCResponse> getABCAnalysis(Long shopId, Long userId) {
        shopService.shopValidationWithUserId(userId, shopId);

        List<TransactionDetail> salesDetails = transactionDetailRepository.findSalesByShopId(shopId);

        Map<String, Long> productValueMap = new HashMap<>();

        for (TransactionDetail td : salesDetails) {
            String key = td.getProductName() != null ? td.getProductName() : "Produk " + td.getProductId();
            Long value = td.getSubTotal() != null ? td.getSubTotal() : 0L;
            productValueMap.put(key, productValueMap.getOrDefault(key, 0L) + value);
        }

        List<Map.Entry<String, Long>> sortedList = productValueMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toList();

        long total = sortedList.stream().mapToLong(Map.Entry::getValue).sum();
        List<ABCResponse> result = new ArrayList<>();
        double cumulative = 0;
        for (Map.Entry<String, Long> entry : sortedList) {

            double ratio = entry.getValue() / (double) total;
            String category;
            if (cumulative + ratio <= 0.7) {
                category = "A";
            } else if (cumulative + ratio <= 0.9) {
                category = "B";
            } else {
                category = "C";
            }
            cumulative += ratio;

            result.add(new ABCResponse(
                    null,              // productId tidak digunakan
                    null,              // variantId tidak digunakan
                    entry.getKey(),    // productName sebagai key
                    entry.getValue(),
                    category
            ));
        }

        return result;
    }

    public List<ABCResponse> getABCAnalysisForRecommendation(Long shopId, Long userId) {
        shopService.shopValidationWithUserId(userId, shopId);

        List<TransactionDetail> salesDetails = transactionDetailRepository.findSalesByShopId(shopId);

        Map<String, ABCResponse> grouped = new HashMap<>();

        for (TransactionDetail td : salesDetails) {
            Long productId = td.getProductId();
            Long variantId = td.getVariantId();
            String key = productId + ":" + (variantId != null ? variantId : "null");
            String name = td.getProductName() != null ? td.getProductName() : "Produk " + productId;

            grouped.compute(key, (k, v) -> {
                if (v == null) {
                    return new ABCResponse(productId, variantId, name, td.getSubTotal(), null);
                } else {
                    v.setTotalValue(v.getTotalValue() + td.getSubTotal());
                    return v;
                }
            });
        }

        List<ABCResponse> sorted = grouped.values().stream()
                .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
                .toList();

        long total = sorted.stream().mapToLong(ABCResponse::getTotalValue).sum();
        double cumulative = 0;
        for (ABCResponse r : sorted) {
            cumulative += (double) r.getTotalValue() / total;
            r.setCategory(cumulative <= 0.7 ? "A" : cumulative <= 0.9 ? "B" : "C");
        }

        return sorted;
    }

    public Map<String, String> getCategoryMap(Long shopId, Long userId) {
        return getABCAnalysisForRecommendation(shopId, userId).stream()
                .collect(Collectors.toMap(
                        r -> r.getProductId() + ":" + (r.getVariantId() != null ? r.getVariantId() : "null"),
                        ABCResponse::getCategory
                ));
    }



}
