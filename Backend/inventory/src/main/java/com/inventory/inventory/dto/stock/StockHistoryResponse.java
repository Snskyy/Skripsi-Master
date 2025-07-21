package com.inventory.inventory.dto.stock;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockHistoryResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private Long stockId;
    private String stockInOut;
    private String stockType;
    private Long quantity;
    private String location;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
    private Long createdBy;
    private Long transactionDetailId;
    private String note;
}
