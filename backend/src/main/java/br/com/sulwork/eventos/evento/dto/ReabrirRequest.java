package br.com.sulwork.eventos.evento.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record ReabrirRequest(
		@NotNull(message = "é obrigatório") Instant prazoAlteracao,
		Instant dataInicio,
		Instant dataFim) {
}
