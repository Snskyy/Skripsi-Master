package com.inventory.inventory.dto.dashboard;

import lombok.Data;

@Data
public class FinanceSummaryResponse {
    private Long totalRevenue;
    private Long totalExpense;
    private Long netProfit;
    private Long profitMargin;
}
