package com.inventory.inventory.service;

import com.inventory.inventory.dto.transaction.FinanceSummaryResponse;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.exception.UserUnauthorizedException;
import com.inventory.inventory.model.Shop;
import com.inventory.inventory.repository.ShopRepository;
import com.inventory.inventory.repository.TransactionHeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Autowired
    private TransactionHeaderRepository transactionHeaderRepository;

    public List<FinanceSummaryResponse> getFinanceSummary(String interval, LocalDate startDate, LocalDate endDate, Long shopId) {

        switch (interval) {
            case "daily":
                return transactionHeaderRepository.findSummaryGroupedByDay(startDate, endDate, shopId);
            case "weekly":
                return transactionHeaderRepository.findSummaryGroupedByWeek(startDate, endDate, shopId);
            case "monthly":
                return transactionHeaderRepository.findSummaryGroupedByMonth(startDate, endDate, shopId);
            case "yearly":
                return transactionHeaderRepository.findSummaryGroupedByYear(startDate, endDate, shopId);
            default:
                throw new IllegalArgumentException("Interval tidak valid: " + interval);
        }

    }



}
