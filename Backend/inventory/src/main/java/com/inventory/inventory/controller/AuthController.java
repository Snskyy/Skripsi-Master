package com.inventory.inventory.controller;
import com.inventory.inventory.dto.auth.*;
import com.inventory.inventory.service.AuthService;
import com.inventory.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
//        authService.registerUser(request);
//        return ResponseEntity.ok("User registered successfully!");
        try {
            authService.registerUser(request);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalArgumentException e) {
            // Catch IllegalArgumentException and return meaningful error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage())); // Return as JSON
        }
    }

//    @PostMapping("/auth/request-reset")
//    public ResponseEntity<?> requestReset(@RequestBody EmailRequest request) {
//        String otp = authService.generateOtpForEmail(request.getEmail());
//        // Kirim OTP via email (gunakan JavaMailSender)
//        return ResponseEntity.ok(Map.of("message", "OTP telah dikirim ke email."));
//    }
//    @PostMapping("/auth/reset-password")
//    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordViaOtpRequest request) {
//        try {
//            userService.resetPasswordByOtp(request);
//            return ResponseEntity.ok("Password berhasil direset.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
//        }
//    }

}
