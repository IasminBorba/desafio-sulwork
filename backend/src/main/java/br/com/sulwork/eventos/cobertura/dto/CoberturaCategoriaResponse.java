package br.com.sulwork.eventos.cobertura.dto;

public record CoberturaCategoriaResponse(
		Long categoriaId,
		String categoriaNome,
		int quantidadeMeta,
		int contribuido,
		int percentual) {
}
