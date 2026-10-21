package br.com.sulwork.eventos.participacao.dto;

import java.util.List;

import br.com.sulwork.eventos.participacao.Participacao;
import br.com.sulwork.eventos.shared.domain.CpfUtils;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.dto.RestricaoResumoResponse;

public record ParticipacaoResponse(
		Long id,
		Long usuarioId,
		String nome,
		String cpf,
		List<RestricaoResumoResponse> restricoes) {

	public static ParticipacaoResponse de(Participacao participacao) {
		Usuario usuario = participacao.getUsuario();
		List<RestricaoResumoResponse> restricoes = usuario.getRestricoes().stream()
				.map(restricao -> new RestricaoResumoResponse(restricao.getId(), restricao.getNome()))
				.toList();
		return new ParticipacaoResponse(
				participacao.getId(),
				usuario.getId(),
				usuario.getNome(),
				CpfUtils.mascarar(usuario.getCpf()),
				restricoes);
	}

}
