package br.com.sulwork.eventos.restricao.dto;

import java.util.List;

public record RestricoesEventoResponse(
		List<RestricaoResumoEventoResponse> resumo,
		List<ItemRestricaoResponse> porItem,
		List<RestricaoSemItemResponse> semItemCompativel) {
}
