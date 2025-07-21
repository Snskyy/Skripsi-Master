package com.inventory.inventory.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String address;
    private LocalDate DOB;
    private String role;
}
