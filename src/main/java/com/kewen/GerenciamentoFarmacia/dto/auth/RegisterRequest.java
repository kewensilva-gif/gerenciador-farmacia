package com.kewen.GerenciamentoFarmacia.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 30) String username,
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Size(min = 6, max = 100) String password
) {
}
