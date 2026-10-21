package br.com.sulwork.eventos.restricao.dto;

import java.util.List;

public record ItemRestricaoResponse(
		Long itemId,
		String nome,
		int quantidadeAfetados,
		List<ParticipanteAfetadoResponse> participantes) {
}
