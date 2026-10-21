package br.com.sulwork.eventos.painel.dto;

public record PainelContribuicaoItemResponse(
		Long itemId,
		String itemNome,
		String categoriaNome,
		int quantidade) {
}
