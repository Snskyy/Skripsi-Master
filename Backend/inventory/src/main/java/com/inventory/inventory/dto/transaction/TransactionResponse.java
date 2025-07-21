package com.inventory.inventory.dto.transaction;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionResponse {
    private Long id;
    private String type;
    private String supplierCustomerName;
    private String note;
    private Long total;
    private String status;
    private String paymentMethod;

    private Long platformId;
    private String platformName;
    private Long supplierCustomerId;
    private Long userId;
    private String createBy;
    private Long shopId;
    private String invoiceNumber;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;

    private List<TransactionDetailResponse> items;
}
