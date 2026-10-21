package br.com.sulwork.eventos.contribuicao.dto;

import br.com.sulwork.eventos.contribuicao.Contribuicao;

public record ContribuicaoResponse(
		Long id,
		Long itemId,
		String itemNome,
		int quantidade,
		Long participacaoId,
		Long usuarioId,
		String usuarioNome) {

	public static ContribuicaoResponse de(Contribuicao contribuicao) {
		return new ContribuicaoResponse(
				contribuicao.getId(),
				contribuicao.getItem().getId(),
				contribuicao.getItem().getNome(),
				contribuicao.getQuantidade(),
				contribuicao.getParticipacao().getId(),
				contribuicao.getParticipacao().getUsuario().getId(),
				contribuicao.getParticipacao().getUsuario().getNome());
	}

}
