package br.com.sulwork.eventos.painel.dto;

import java.util.List;

import br.com.sulwork.eventos.usuario.dto.RestricaoResumoResponse;

public record PainelParticipanteResponse(
		Long participacaoId,
		Long usuarioId,
		String nome,
		String cpf,
		boolean semContribuicao,
		List<RestricaoResumoResponse> restricoes,
		List<PainelContribuicaoItemResponse> contribuicoes) {
}
