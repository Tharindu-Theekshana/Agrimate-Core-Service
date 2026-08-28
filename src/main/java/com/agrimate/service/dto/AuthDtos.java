package com.agrimate.service.dto;

import com.agrimate.service.model.role.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 30)
            @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may only contain letters, numbers, . _ -")
            String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
            @NotBlank String name,
            String phone,
            String location,
            RoleName role
    ) {}

    public record LoginRequest(
            @NotBlank String identifier,
            @NotBlank String password
    ) {}

    public record RefreshRequest(String refreshToken) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            UserDto user
    ) {}

    public record SendOtpRequest(
            @NotBlank @Email String email
    ) {}

    public record RegisterOtpRequest(
            @NotBlank @Size(min = 3, max = 30)
            @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may only contain letters, numbers, . _ -")
            String username,
            @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank @Email String email,
            @NotBlank String code,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword
    ) {}
}
