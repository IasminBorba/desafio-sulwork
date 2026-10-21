package br.com.sulwork.eventos.auth.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiraEm) {
}
