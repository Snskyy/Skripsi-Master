package com.inventory.inventory.dto.transaction;

import lombok.Data;

@Data
public class TransactionStatusRequest {
    private String note;
    private String status;
}
