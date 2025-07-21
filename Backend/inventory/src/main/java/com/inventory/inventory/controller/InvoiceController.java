package com.inventory.inventory.controller;

import com.inventory.inventory.model.User;
import com.inventory.inventory.service.ShopService;
import com.inventory.inventory.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ShopService shopService;


    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable String invoiceNumber,
            @AuthenticationPrincipal User user,
            @RequestHeader("shop-id") Long shopId) {
        shopService.shopValidationWithUserId(user.getId(), shopId);
        byte[] pdf = transactionService.generateInvoicePdf(invoiceNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoiceNumber + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
