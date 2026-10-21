package br.com.sulwork.eventos.contribuicao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContribuicaoRequest(
		@NotNull(message = "é obrigatório") Long itemId,
		@NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer quantidade) {
}
