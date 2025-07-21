package com.inventory.inventory.dto.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ShopUpdateRequest {
    private String name;
    private String phone;
    private String address;
    private String email;
}
