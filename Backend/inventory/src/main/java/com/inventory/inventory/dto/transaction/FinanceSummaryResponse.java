package com.inventory.inventory.dto.transaction;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryResponse {
    private String date; // Tanggal, Minggu, Bulan, atau Tahun
    private BigDecimal revenue;      // ✅ WAS Long
    private BigDecimal expenditure;  // ✅ WAS Long
}
