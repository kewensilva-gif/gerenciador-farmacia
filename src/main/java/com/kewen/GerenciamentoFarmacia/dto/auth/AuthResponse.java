package com.kewen.GerenciamentoFarmacia.dto.auth;

public record AuthResponse(
        String token,
        String username,
        String email
) {
}
