package com.inventory.inventory.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FinanceChartResponse {
    private List<String> labels;
    private List<Long> revenue;
    private List<Long> expense;
    private List<Long> netProfit;
    private List<Double> profitMargin;
}