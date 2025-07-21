package com.inventory.inventory.dto.shop;

import lombok.Data;

@Data
public class ShopResponse {
    private long id;
    private Long userId;
    private String name;
    private String address;
    private String phone;
    private String email;
}
