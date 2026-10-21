package br.com.sulwork.eventos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "é obrigatório") @Email(message = "e-mail inválido") String email,
		@NotBlank(message = "é obrigatório") String senha) {
}
