package com.inventory.inventory.controller;

import com.inventory.inventory.dto.auth.ResetPasswordRequest;
import com.inventory.inventory.exception.NotFoundException;
import com.inventory.inventory.model.User;
import com.inventory.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevToolsController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetUserPassword(@RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String hashed = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashed);
        userRepository.save(user);

        System.out.println("✅ Password reset untuk user: " + user.getEmail());
        return ResponseEntity.ok("Password berhasil di-reset.");
    }
}
