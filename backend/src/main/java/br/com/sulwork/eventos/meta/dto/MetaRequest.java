package br.com.sulwork.eventos.meta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MetaRequest(
		@NotNull(message = "é obrigatório") Long categoriaId,
		@NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer quantidadeMeta) {
}
