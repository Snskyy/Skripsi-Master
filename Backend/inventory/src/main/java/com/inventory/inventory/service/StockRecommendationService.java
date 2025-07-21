package com.inventory.inventory.service;

import com.inventory.inventory.dto.stock.StockRecommendationResponse;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.model.ProductVariant;
import com.inventory.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StockRecommendationService {
    @Autowired
    private TransactionHeaderRepository transactionHeaderRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private AnalysisService analysisService;

    private static final int LEAD_TIME_DAYS = 14;
    private static final double SAFETY_STOCK_PERCENT = 0.2;
    private static final int MAX_TURNOVER_DAYS = 60;
    private static final double STOCK_SURPLUS_THRESHOLD = 5.0;
    private static final double STOCK_NORMAL_THRESHOLD = 3.0;
    private static final double MIN_STOCK_GAP_PERCENT = 1.2;


    private static final int MIN_SALES_30DAYS = 5;
    private static final int MIN_ACTIVE_DAYS = 2;

    private static final Map<LocalDate, String> HOLIDAY_EVENTS = Map.ofEntries(
            Map.entry(LocalDate.of(2025, 1, 1), "Tahun Baru"),
            Map.entry(LocalDate.of(2025, 2, 2), "Tanggal Cantik 2.2"),
            Map.entry(LocalDate.of(2025, 2, 14), "Valentine"),
            Map.entry(LocalDate.of(2025, 3, 3), "Tanggal Cantik 3.3"),
            Map.entry(LocalDate.of(2025, 4, 4), "Tanggal Cantik 4.4"),
            Map.entry(LocalDate.of(2025, 4, 10), "Lebaran"),
            Map.entry(LocalDate.of(2025, 5, 5), "Tanggal Cantik 5.5"),
//            Map.entry(LocalDate.of(2025, 5, 25), "test tanggal 25"),
            Map.entry(LocalDate.of(2025, 6, 6), "Tanggal Cantik 6.6"),
            Map.entry(LocalDate.of(2025, 7, 7), "Tanggal Cantik 7.7"),
            Map.entry(LocalDate.of(2025, 8, 8), "Tanggal Cantik 8.8"),
            Map.entry(LocalDate.of(2025, 9, 9), "Tanggal Cantik 9.9"),
            Map.entry(LocalDate.of(2025, 10, 10), "Tanggal Cantik 10.10"),
            Map.entry(LocalDate.of(2025, 11, 11), "Tanggal Cantik 11.11"),
            Map.entry(LocalDate.of(2025, 12, 12), "Tanggal Cantik 12.12"),
            Map.entry(LocalDate.of(2025, 12, 25), "Natal")
    );
    public List<StockRecommendationResponse> generateStockRecommendations(Long userId, Long shopId) {
        List<StockRecommendationResponse> recommendations = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        Map<String, String> abcCategoryMap = analysisService.getCategoryMap(shopId, userId);

        List<Product> products = productRepository.findActiveByShopId(shopId);
        for (Product product : products) {
            if (Boolean.TRUE.equals(product.getHasVariant())) {
                List<ProductVariant> variants = productVariantRepository.findByProductIdAndDeleteDateIsNull(product.getId());
                for (ProductVariant variant : variants) {
                    processRecommendation(shopId, product, variant, today, todayStart, recommendations,abcCategoryMap);
                }
            } else {
                processRecommendation(shopId, product, null, today, todayStart, recommendations,abcCategoryMap);
            }
        }
        return recommendations;
    }

    private void processRecommendation(Long shopId, Product product, ProductVariant variant,
                                       LocalDate today, LocalDateTime todayStart,
                                       List<StockRecommendationResponse> recs,
                                       Map<String, String> abcCategoryMap) {

        Long productId = product.getId();
        Long variantId = variant != null ? variant.getId() : null;
        boolean isWithVariant = Boolean.TRUE.equals(product.getHasVariant());
        Long variantIdForQuery = isWithVariant ? null : variantId;

        Long sales30 = transactionDetailRepository.sumSalesBetweenValidStatus(productId, variantIdForQuery, shopId, todayStart.minusDays(30));
        Long sales7 = transactionDetailRepository.sumSalesBetweenValidStatus(productId, variantIdForQuery, shopId, todayStart.minusDays(7));

        if (sales30 == null || sales30 < MIN_SALES_30DAYS) return;

        int activeDays = transactionDetailRepository.countActiveSaleDays(productId, variantIdForQuery, shopId, todayStart.minusDays(30));
        if (activeDays < MIN_ACTIVE_DAYS) return;

        double avg30 = sales30 / 30.0;
        double avg7 = (sales7 != null ? sales7 : 0) / 7.0;
        double avgDaily = (avg7 * 0.6) + (avg30 * 0.4);

        long leadTimeDemand = Math.round(avgDaily * LEAD_TIME_DAYS);
        long safetyStock = Math.round(leadTimeDemand * SAFETY_STOCK_PERCENT);
        long recommendedMin = leadTimeDemand + safetyStock;

        // Ambil kategori ABC dari map
        String abcKey = productId + ":" + (variantId != null ? variantId : "null");
        String abcCategory = abcCategoryMap.getOrDefault(abcKey, "C");

        // Modifikasi stok minimum berdasarkan kategori ABC
        double multiplier = switch (abcCategory) {
            case "A" -> 1.2;
            case "B" -> 1.1;
            default -> 1.0;
        };
        long adjustedMin = Math.round(recommendedMin * multiplier);

        Long currentStock = (variantId != null)
                ? stockRepository.sumQuantityByVariantId(variantId)
                : stockRepository.sumQuantityByProductId(productId);
        if (currentStock == null) currentStock = 0L;

        double turnoverDays = avgDaily > 0 ? currentStock / avgDaily : 999;

        Long currentMin = (variant != null) ? variant.getMinimumStock() : product.getMinimumStock();
        if (currentMin == null) currentMin = 0L;

        long diffMin = Math.abs(adjustedMin - currentMin);
        boolean significantGap = diffMin >= 3 || diffMin >= currentMin * 0.1;

        boolean triggerDueToLowStock = currentStock < adjustedMin * STOCK_NORMAL_THRESHOLD;
        boolean triggerDueToAdjustment = avgDaily > currentMin * MIN_STOCK_GAP_PERCENT;

        // (Opsional) Skip produk C jika stok aman
        if ("C".equals(abcCategory) && currentStock >= adjustedMin * 1.5 && !triggerDueToAdjustment) return;

        if (currentStock >= adjustedMin * STOCK_SURPLUS_THRESHOLD && !triggerDueToAdjustment) return;
        if (turnoverDays > MAX_TURNOVER_DAYS && !triggerDueToAdjustment) return;
        if (!triggerDueToLowStock && !triggerDueToAdjustment) return;
        if (!significantGap) return;

        String trendReason = analyzeTrend(avg7, avg30, currentMin, adjustedMin);

        String eventReason = detectHolidayEvent(today, productId, variantId, shopId);
        String fullReason = trendReason + (eventReason.isEmpty() ? "" : " | " + eventReason);

        if (!Objects.equals(currentMin, adjustedMin)) {
//            String key = productId + ":" + (variantId != null ? variantId : "null");
//            notificationService.createOnce(shopId, "STOCK_RECOMMENDATION", key,
//                    "Rekomendasi minimum stok",
//                    "Produk " + product.getName() + (variant != null ? " (" + variant.getAttributes() + ")" : "") + " disarankan penyesuaian stok.",
//                    LocalDateTime.now().plusDays(14));

            StockRecommendationResponse r = new StockRecommendationResponse();
            r.setProductId(productId);
            r.setVariantId(variantId);
            r.setProductName(product.getName());
            r.setSku(variant != null ? variant.getSku() : null);
            r.setAttributes(variant != null ? variant.getAttributes() : null);
            r.setMinimumStock(currentMin);
            r.setRecommendedMinimumStock(adjustedMin);
            r.setRecommendationReason(fullReason);
            r.setAbcCategory(abcCategory); // ✅ tambahkan ke DTO
            recs.add(r);
        }
//        System.out.println("=== DEBUG STOCK REKOMENDASI ===");
//        System.out.println("Product ID     : " + productId);
//        System.out.println("Variant ID     : " + variantId);
//        System.out.println("Product Name   : " + product.getName());
//        System.out.println("Category ABC   : " + abcCategory);
//        System.out.println("Sales30        : " + sales30);
//        System.out.println("Sales7         : " + sales7);
//        System.out.println("Avg Daily      : " + avgDaily);
//        System.out.println("Lead Time Dem  : " + leadTimeDemand);
//        System.out.println("Safety Stock   : " + safetyStock);
//        System.out.println("Base Min Stock : " + recommendedMin);
//        System.out.println("Adjusted Min   : " + adjustedMin);
//        System.out.println("Current Min    : " + currentMin);
//        System.out.println("Current Stock  : " + currentStock);
//        System.out.println("Turnover Days  : " + turnoverDays);
//        System.out.println("Significant Gap: " + significantGap);
//        System.out.println("Reason Trend   : " + trendReason);
//        System.out.println("Reason Event   : " + eventReason);
//        System.out.println("Full Reason    : " + fullReason);
//        System.out.println("===============================");

    }



    private String analyzeTrend(double avg7, double avg30, long currentMin, long adjustedMin) {
        if (avg30 == 0 && avg7 == 0) return "Tidak ada penjualan";
        if (avg30 == 0 && avg7 > 0) return "Penjualan baru muncul setelah periode kosong";

        double diff = avg7 - avg30;
        double percent = (diff / avg30) * 100;

        boolean stokMinimumTurun = adjustedMin < currentMin;
        if (stokMinimumTurun) {
            return String.format("Rekomendasi stok turun karena penjualan menurun dibanding rata-rata 30 hari", -percent);
        }
        if (percent >= 300) return "Penjualan meningkat tajam setelah periode sepi";
        if (percent >= 30) return String.format("Penjualan meningkat %.0f%% dibanding rata-rata 30 hari", percent);
        if (percent <= -50) return String.format("Penjualan anjlok %.0f%% dibanding rata-rata 30 hari", -percent);
        if (percent <= -30) return String.format("Penjualan menurun %.0f%% dibanding rata-rata 30 hari", -percent);
        if (percent >= 10) return String.format("Penjualan meningkat %.0f%% dibanding rata-rata 30 hari", percent);

        return "Tren penjualan stabil (±10%)";
    }


    private String detectHolidayEvent(LocalDate today, Long productId, Long variantId, Long shopId) {
        for (Map.Entry<LocalDate, String> e : HOLIDAY_EVENTS.entrySet()) {
            LocalDate holiday = e.getKey();
            String event = e.getValue();

            if (today.isAfter(holiday.minusDays(14)) && today.isBefore(holiday)) {
                return "Menjelang " + event + ", disarankan naikkan stok untuk antisipasi lonjakan";
            }

            if (today.isAfter(holiday.plusDays(1)) && today.isBefore(holiday.plusDays(4))) {
                Double avgBefore = transactionDetailRepository.avgSalesBetweenValidStatus(
                        productId, variantId, shopId,
                        holiday.minusDays(7).atStartOfDay(),
                        holiday.minusDays(1).atTime(LocalTime.MAX));

                Double avgAfter = transactionDetailRepository.avgSalesBetweenValidStatus(
                        productId, variantId, shopId,
                        holiday.plusDays(1).atStartOfDay(),
                        holiday.plusDays(3).atTime(LocalTime.MAX));

                if (avgBefore != null && avgAfter != null && avgAfter < avgBefore) {
                    double drop = ((avgBefore - avgAfter) / avgBefore) * 100;
                    return String.format("Penjualan sehabis %s menurun sekitar %.0f%%", event, drop);
                }
            }
        }
        return "";
    }

}
