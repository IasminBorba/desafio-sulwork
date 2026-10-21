package br.com.sulwork.eventos.cobertura.dto;

public record CoberturaItemResponse(
		Long itemId,
		String nome,
		String categoriaNome,
		boolean obrigatorio,
		int quantidadeNecessaria,
		int contribuido,
		int faltante,
		int percentual) {
}
