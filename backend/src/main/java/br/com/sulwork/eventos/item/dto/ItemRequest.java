package br.com.sulwork.eventos.item.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ItemRequest(
		@NotBlank(message = "é obrigatório") @Size(max = 150) String nome,
		@NotNull(message = "é obrigatório") Long categoriaId,
		@NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer quantidadeNecessaria,
		boolean obrigatorio,
		List<Long> alergenoIds) {
}
