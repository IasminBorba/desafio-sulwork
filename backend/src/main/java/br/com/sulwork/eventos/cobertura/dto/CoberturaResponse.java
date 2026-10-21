package br.com.sulwork.eventos.cobertura.dto;

import java.util.List;

public record CoberturaResponse(
		int percentualGeral,
		double a,
		Double b,
		Double c,
		List<CoberturaCategoriaResponse> categorias,
		List<CoberturaItemResponse> itens,
		List<CoberturaItemResponse> obrigatoriosPendentes) {
}
