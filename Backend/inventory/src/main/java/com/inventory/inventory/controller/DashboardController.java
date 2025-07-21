package com.inventory.inventory.controller;

import com.inventory.inventory.dto.dashboard.ABCResponse;
import com.inventory.inventory.dto.dashboard.FinanceChartResponse;
import com.inventory.inventory.dto.dashboard.FinanceSummaryResponse;
import com.inventory.inventory.dto.dashboard.LastTransactionResponse;
import com.inventory.inventory.dto.dashboard.TopProductResponse;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.AnalysisService;
import com.inventory.inventory.service.DashboardService;
import com.inventory.inventory.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
//    @Autowired
//    private NotificationService notificationService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/finance-summary")
    public FinanceSummaryResponse getOverallFinanceSummary(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        return dashboardService.getOverallFinanceSummary(shopId, user.getId());
    }

    @GetMapping("/notifications")
    public Map<String, Integer> getDashboardNotifications(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        return dashboardService.generateRealTimeNotificationCount(shopId);
    }

    @GetMapping("/top-products")
    public List<TopProductResponse> getTopProducts(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        return dashboardService.getTopSellingProducts(shopId, user.getId());
    }

    @GetMapping("/last-transaction")
    public List<LastTransactionResponse> getLastTransaction(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        return dashboardService.getRecentTransactions(shopId, user.getId());
    }

    @GetMapping("/finance-chart")
    public FinanceChartResponse getFinanceChart(
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        return dashboardService.getWeeklyFinanceChart(shopId, user.getId());
    }

    @GetMapping("/abc-analysis")
    public ResponseEntity<List<ABCResponse>> getABCData(
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analysisService.getABCAnalysis(shopId, user.getId()));
    }



}
