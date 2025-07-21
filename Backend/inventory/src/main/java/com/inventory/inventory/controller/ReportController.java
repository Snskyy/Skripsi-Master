package com.inventory.inventory.controller;

import com.inventory.inventory.dto.transaction.FinanceSummaryResponse;
import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ReportService;
import com.inventory.inventory.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ShopService shopService;

    @GetMapping
    public ResponseEntity<?> getFinanceReport(
            @RequestParam String interval,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("shop-id") Long shopId,
            @AuthenticationPrincipal User user) {

        shopService.shopValidationWithUserId(user.getId(), shopId);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
        LocalDate start = null;
        LocalDate end = null;

        try {
            if (interval.equals("daily")) {
                if (startDate != null) start = LocalDate.parse(startDate, dateFormatter);
                if (endDate != null) end = LocalDate.parse(endDate, dateFormatter).plusDays(1); // include endDate
            } else if (interval.equals("monthly")) {
                if (startDate != null) start = LocalDate.parse(startDate + "-01", dateFormatter);
                if (endDate != null) end = LocalDate.parse(endDate + "-01", dateFormatter).plusMonths(1); // include end month
            } else if (interval.equals("yearly")) {
                if (startDate != null) start = LocalDate.of(Integer.parseInt(startDate), 1, 1);
                if (endDate != null) end = LocalDate.of(Integer.parseInt(endDate) + 1, 1, 1); // year+1
            } else if (interval.equals("weekly")) {
                WeekFields weekFields = WeekFields.ISO;

                if (startDate != null) {
                    String[] parts = startDate.split("-W");
                    int year = Integer.parseInt(parts[0]);
                    int week = Integer.parseInt(parts[1]);
                    start = LocalDate.ofYearDay(year, 1)
                            .with(weekFields.weekOfYear(), week)
                            .with(weekFields.dayOfWeek(), 1);
                }

                if (endDate != null) {
                    String[] parts = endDate.split("-W");
                    int year = Integer.parseInt(parts[0]);
                    int week = Integer.parseInt(parts[1]);
                    end = LocalDate.ofYearDay(year, 1)
                            .with(weekFields.weekOfYear(), week)
                            .with(weekFields.dayOfWeek(), 1)
                            .plusWeeks(1); // supaya inclusive
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Format tanggal tidak valid untuk interval " + interval);
        }

        // ✅ Panggil service dan kembalikan response
        List<FinanceSummaryResponse> result = reportService.getFinanceSummary(interval, start, end, shopId);
        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

}
