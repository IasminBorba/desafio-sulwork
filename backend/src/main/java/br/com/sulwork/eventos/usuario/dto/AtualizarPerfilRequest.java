package br.com.sulwork.eventos.usuario.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(
		@NotBlank(message = "é obrigatório") @Size(max = 150) String nome,
		List<Long> restricaoIds) {
}
