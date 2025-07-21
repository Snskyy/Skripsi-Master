package com.inventory.inventory.dto.cart;


import lombok.Data;

import java.util.List;

@Data
public class ListCartResponse {
    private List<CartResponse> items;
    private Long total;  // Sum of all subtotals
}