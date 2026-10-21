package br.com.sulwork.eventos.auth.dto;

import java.util.List;

import br.com.sulwork.eventos.shared.validation.CpfValido;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
		@NotBlank(message = "é obrigatório") @Size(max = 150) String nome,
		@NotBlank(message = "é obrigatório") @CpfValido String cpf,
		@NotBlank(message = "é obrigatório") @Email(message = "e-mail inválido") String email,
		@NotBlank(message = "é obrigatório") @Size(min = 8, message = "deve ter no mínimo 8 caracteres") String senha,
		List<Long> restricaoIds) {
}
