package com.inventory.inventory.service;

import com.inventory.inventory.dto.dashboard.FinanceChartResponse;
import com.inventory.inventory.dto.dashboard.FinanceSummaryResponse;
import com.inventory.inventory.dto.dashboard.LastTransactionResponse;
import com.inventory.inventory.dto.dashboard.TopProductResponse;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.model.TransactionDetail;
import com.inventory.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    @Autowired
    private  ShopRepository shopRepository;

    @Autowired
    private TransactionHeaderRepository transactionHeaderRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    public FinanceSummaryResponse getOverallFinanceSummary(Long shopId, Long userId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));

        if (!shop.getUserId().equals(userId)) {
            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
        }

        Long totalSales = transactionHeaderRepository.sumTotalByTypeAndStatus(shopId, "SALES", "FINAL");
        Long totalPurchase = transactionHeaderRepository.sumTotalByTypeAndStatus(shopId, "PURCHASE", "FINAL");

        totalSales = totalSales != null ? totalSales : 0L;
        totalPurchase = totalPurchase != null ? totalPurchase : 0L;

        Long profit = totalSales - totalPurchase;
        double margin = totalSales > 0 ? (double) profit * 100 / totalSales : 0.0;

        FinanceSummaryResponse summary = new FinanceSummaryResponse();
        summary.setTotalRevenue(totalSales);
        summary.setTotalExpense(totalPurchase);
        summary.setNetProfit(profit);
        summary.setProfitMargin(Math.round(margin));

        return summary;
    }
    public List<TopProductResponse> getTopSellingProducts(Long shopId, Long userId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));
        if (!shop.getUserId().equals(userId)) {
            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
        }
        List<Object[]> results = transactionDetailRepository.findTopSellingProductsFiltered(shopId);
        List<TopProductResponse> topProducts = results.stream()
                .map(row -> {
                    try {
                        Long productId = ((Number) row[0]).longValue();
                        Long quantitySold = ((Number) row[1]).longValue();
                        Optional<Product> productOpt = productRepository.findById(productId);

                        if (productOpt.isEmpty()) {
                            return null;
                        }

                        Product product = productOpt.get();

                        if (!product.getShopId().equals(shopId)) {
                            return null;
                        }

                        if (product.getDeleteDate() != null) {
                            return null;
                        }

                        return new TopProductResponse(product.getName(), quantitySold);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .limit(5)
                .collect(Collectors.toList());
        return topProducts;
    }


    public List<LastTransactionResponse> getRecentTransactions(Long shopId, Long userId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));

        if (!shop.getUserId().equals(userId)) {
            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
        }

        List<Object[]> rows = transactionHeaderRepository.findLastSalesTransaction(shopId);
        List<LastTransactionResponse> responses = new ArrayList<>();

        for (int i = 0; i < Math.min(5, rows.size()); i++) {
            Object[] row = rows.get(i);
            Long transactionId = (Long) row[0];
            String invoice = (String) row[1];
            LocalDate date = ((LocalDateTime) row[2]).toLocalDate();

            List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(transactionId);
            long totalQty = details.stream().mapToLong(TransactionDetail::getQuantity).sum();
            long totalPrice = details.stream().mapToLong(TransactionDetail::getSubTotal).sum();

            responses.add(new LastTransactionResponse(date, invoice, totalQty, totalPrice));
        }

        return responses;
    }


    public FinanceChartResponse getWeeklyFinanceChart(Long shopId, Long userId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop tidak ditemukan"));

        if (!shop.getUserId().equals(userId)) {
            throw new UserUnauthorizedException("Akses ditolak untuk shop ini.");
        }
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 7 hari termasuk hari ini

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        Map<String, Long> revenueMap = transactionHeaderRepository.sumRevenueByDate(shopId, start, end).stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));

        Map<String, Long> expenseMap = transactionHeaderRepository.sumExpenseByDate(shopId, start, end).stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));

        Set<String> allDates = new TreeSet<>();
        allDates.addAll(revenueMap.keySet());
        allDates.addAll(expenseMap.keySet());

        List<String> labels = new ArrayList<>();
        List<Long> revenue = new ArrayList<>();
        List<Long> expense = new ArrayList<>();
        List<Long> netProfit = new ArrayList<>();
        List<Double> profitMargin = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString();
            long rev = revenueMap.getOrDefault(dateStr, 0L);
            long exp = expenseMap.getOrDefault(dateStr, 0L);
            long profit = rev - exp;
            double margin = rev > 0 ? ((double) profit * 100 / rev) : 0.0;

            labels.add(dateStr);
            revenue.add(rev);
            expense.add(exp);
            netProfit.add(profit);
            profitMargin.add(Math.round(margin * 10.0) / 10.0);
        }

        return new FinanceChartResponse(labels, revenue, expense, netProfit, profitMargin);
    }


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
}
