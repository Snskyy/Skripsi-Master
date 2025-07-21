package com.inventory.inventory.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "users") // Hindari pakai nama "user" karena itu keyword di banyak DB
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String gender;
    private String address;

    @Column(name = "dob")
    private LocalDate DOB;

    private String otp;
    private LocalDateTime otpExpiry;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String role;

}


