package com.inventory.inventory.dto.user;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {

    private String name;
    private Long shopId;
    private String email;
    private String phone;
    private String gender;
    private String address;
    private LocalDate dob;
    private LocalDateTime createDate;
    private String role;

}
