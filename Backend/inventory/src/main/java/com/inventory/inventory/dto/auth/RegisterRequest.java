package com.inventory.inventory.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

    @Data
    public class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String phone;
        private String gender;
        private String address;

        @JsonProperty("DOB")
        private LocalDate DOB;

        @JsonProperty("shopName")
        private String shopName;
        private String shopUrl;
        @JsonProperty("shopAddress")
        private String shopAddress;

        @JsonProperty("shopPhone")
        private String shopPhone;

        @JsonProperty("shopEmail")
        private String shopEmail;
    }
