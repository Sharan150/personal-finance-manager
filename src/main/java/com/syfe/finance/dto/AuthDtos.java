package com.syfe.finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Email String username,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String fullName,
            @NotBlank String phoneNumber
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String username,
            @NotBlank String password
    ) {
    }

    public record RegisterResponse(String message, Long userId) {
    }

    public record MessageResponse(String message) {
    }
}
