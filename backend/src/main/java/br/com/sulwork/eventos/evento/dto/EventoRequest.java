package br.com.sulwork.eventos.evento.dto;

import java.time.Instant;

import br.com.sulwork.eventos.evento.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventoRequest(
		@NotBlank(message = "é obrigatório") @Size(max = 150) String nome,
		@NotBlank(message = "é obrigatório") String descricao,
		@NotNull(message = "é obrigatório") TipoEvento tipo,
		@NotNull(message = "é obrigatório") Instant dataInicio,
		@NotNull(message = "é obrigatório") Instant dataFim,
		@NotNull(message = "é obrigatório") Instant prazoAlteracao) {
}
