package br.com.sulwork.eventos.usuario.dto;

import java.util.List;

import br.com.sulwork.eventos.usuario.Usuario;

public record UsuarioResponse(
		Long id,
		String nome,
		String cpf,
		String email,
		List<RestricaoResumoResponse> restricoes) {

	public static UsuarioResponse de(Usuario usuario) {
		List<RestricaoResumoResponse> restricoes = usuario.getRestricoes().stream()
				.map(restricao -> new RestricaoResumoResponse(restricao.getId(), restricao.getNome()))
				.toList();
		String cpf = usuario.getCpf() == null ? null : usuario.getCpf().strip();
		return new UsuarioResponse(usuario.getId(), usuario.getNome(), cpf, usuario.getEmail(), restricoes);
	}

}
