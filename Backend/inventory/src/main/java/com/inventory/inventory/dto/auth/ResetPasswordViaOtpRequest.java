package com.inventory.inventory.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordViaOtpRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}
