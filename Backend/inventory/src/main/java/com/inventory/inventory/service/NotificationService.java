package com.inventory.inventory.service;

import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.*;
import com.inventory.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionHeaderRepository transactionHeaderRepository;

    @Autowired
    private StockRepository stockRepository;

    public Map<String, Integer> generateRealTimeNotificationCount(Long shopId) {

        Map<String, Integer> result = new HashMap<>();

        int uncat = productRepository.countByShopIdAndCategoryIdIsNullAndDeleteDateIsNull(shopId);
        result.put("uncategorizedProducts", uncat);

        int pendingTx = transactionHeaderRepository.countByShopIdAndStatus(shopId, "PENDING");
        result.put("transactionsPending", pendingTx);

        int receivedPurchase = transactionHeaderRepository.countReceivedPurchaseTransactions(shopId);
        result.put("purchaseToAssign", receivedPurchase);

        List<Object[]> summaries = stockRepository.fetchStockSummary(shopId);
        int restock = 0;
        int attention = 0;
        for (Object[] row : summaries) {
            Long minimum = ((Number) row[2]).longValue();
            Long available = ((Number) row[3]).longValue();
            if (minimum > 0 && available < minimum) {
                restock++;
            } else if (minimum > 0 && available < 3 * minimum) {
                attention++;
            }
        }
        result.put("restockNeeded", restock);
        result.put("stockWarnings", attention);
        return result;
    }

//    public void createOnce(Long shopId, String type, String key, String title, String content, LocalDateTime expireAt) {
//        boolean exists = notificationRepo.existsByShopIdAndTypeAndKey(shopId, type, key);
//        if (!exists) {
//            DashboardNotification notif = new DashboardNotification();
//            notif.setShopId(shopId);
//            notif.setType(type);
//            notif.setKey(key);
//            notif.setTitle(title);
//            notif.setContent(content);
//            notif.setCreateDate(LocalDateTime.now());
//            notif.setExpireDate(expireAt);
//            notif.setResolved(false);
//            notif.setRead(false);
//            notificationRepo.save(notif);
//        }
//    }
//
//    public void markAsResolvedByKey(Long shopId, String type, String key) {
//        List<DashboardNotification> notifs = notificationRepo.findByShopIdAndTypeAndResolvedFalse(shopId, type);
//        for (DashboardNotification n : notifs) {
//            if (key.equals(n.getKey())) {
//                n.setResolved(true);
//                n.setRead(true);
//                notificationRepo.save(n);
//            }
//        }
//    }

//    public void markAsRead(Long id, Long shopId) {
//        DashboardNotification notif = notificationRepo.findByIdAndShopId(id, shopId)
//                .orElseThrow(() -> new NotFoundException("Notifikasi tidak ditemukan"));
//        notif.setRead(true);
//        notificationRepo.save(notif);
//    }

//    public List<DashboardNotification> getActiveNotifications(Long shopId) {
//        return notificationRepo.findByShopIdAndResolvedFalseAndExpireDateAfter(shopId, LocalDateTime.now());
//    }
//    public Map<String, Integer> getSummary(Long shopId) {
//        List<DashboardNotification> active = notificationRepo
//                .findByShopIdAndResolvedFalseAndExpireDateAfter(shopId, LocalDateTime.now());
//
//        Map<String, Integer> summary = new HashMap<>();
//        summary.put("stockRecommendations", 0);
//        summary.put("transactionsPending", 0);
//        summary.put("uncategorizedProducts", 0);
//        summary.put("holidayReminders", 0);
//
//        for (DashboardNotification notif : active) {
//            switch (notif.getType()) {
//                case "STOCK_RECOMMENDATION" -> summary.computeIfPresent("stockRecommendations", (k, v) -> v + 1);
//                case "TRANSACTION_PENDING" -> summary.computeIfPresent("transactionsPending", (k, v) -> v + 1);
//                case "UNCATEGORIZED_PRODUCT" -> summary.computeIfPresent("uncategorizedProducts", (k, v) -> v + 1);
//                case "HOLIDAY_REMINDER" -> summary.computeIfPresent("holidayReminders", (k, v) -> v + 1);
//            }
//        }
//
//        return summary;
//    }
//    public void evaluateGlobalNotifications(Long shopId) {
//        checkUncategorizedProducts(shopId);
//        checkLowStock(shopId);
//        checkPendingTransactions(shopId);
//        // Bisa tambah checkAssignableTransaction(), checkHolidayReminder(), dll.
//    }
//    private void checkUncategorizedProducts(Long shopId) {
//        List<Product> products = productRepository.findByShopIdAndCategoryIdIsNullAndDeleteDateIsNull(shopId);
//        for (Product product : products) {
//            createOnce(
//                    shopId,
//                    "UNCATEGORIZED_PRODUCT",
//                    "product-" + product.getId(),
//                    "Produk tanpa kategori",
//                    "Produk \"" + product.getName() + "\" belum memiliki kategori.",
//                    LocalDateTime.now().plusDays(7)
//            );
//        }
//    }
//    private void checkLowStock(Long shopId) {
//        List<Product> products = productRepository.findActiveByShopId(shopId);
//
//        for (Product product : products) {
//            if (Boolean.TRUE.equals(product.getHasVariant())) {
//                List<ProductVariant> variants = productVariantRepository.findByProductIdAndDeleteDateIsNull(product.getId());
//                for (ProductVariant variant : variants) {
//                    long stock = stockRepository.sumAvailableStockByVariantId(variant.getId()) != null
//                            ? stockRepository.sumAvailableStockByVariantId(variant.getId())
//                            : 0L;
//                    long min = variant.getMinimumStock() != null ? variant.getMinimumStock() : 0L;
//
//                    String key = "variant-" + variant.getId();
//                    if (stock < min) {
//                        createOnce(
//                                shopId,
//                                "STOCK_RECOMMENDATION",
//                                key,
//                                "Stok varian rendah",
//                                "Varian SKU \"" + variant.getSku() + "\" berada di bawah minimum.",
//                                LocalDateTime.now().plusDays(7)
//                        );
//                    } else {
//                        markAsResolvedByKey(shopId, "STOCK_RECOMMENDATION", key);
//                    }
//                }
//            } else {
//                long stock = stockRepository.sumAvailableStockByProductId(product.getId()) != null
//                        ? stockRepository.sumAvailableStockByProductId(product.getId())
//                        : 0L;
//                long min = product.getMinimumStock() != null ? product.getMinimumStock() : 0L;
//
//                String key = "product-" + product.getId();
//                if (stock < min) {
//                    createOnce(
//                            shopId,
//                            "STOCK_RECOMMENDATION",
//                            key,
//                            "Stok produk rendah",
//                            "Stok produk \"" + product.getName() + "\" berada di bawah minimum.",
//                            LocalDateTime.now().plusDays(7)
//                    );
//                } else {
//                    markAsResolvedByKey(shopId, "STOCK_RECOMMENDATION", key);
//                }
//            }
//        }
//    }
//
//    private void checkPendingTransactions(Long shopId) {
//        List<TransactionHeader> pendingTx = transactionHeaderRepository.findByShopIdAndStatus(shopId, "PENDING");
//        for (TransactionHeader tx : pendingTx) {
//            createOnce(
//                    shopId,
//                    "TRANSACTION_PENDING",
//                    "invoice-" + tx.getInvoiceNumber(),
//                    "Transaksi Pending",
//                    "Transaksi \"" + tx.getInvoiceNumber() + "\" masih berstatus PENDING.",
//                    LocalDateTime.now().plusDays(3)
//            );
//        }
//    }



}
