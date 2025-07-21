package com.inventory.inventory.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LastTransactionResponse {
    private LocalDate date;
    private String invoiceNumber;
    private Long totalQuantity;
    private Long totalPrice;
}
