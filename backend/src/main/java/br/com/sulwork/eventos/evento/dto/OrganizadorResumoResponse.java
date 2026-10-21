package br.com.sulwork.eventos.evento.dto;

import br.com.sulwork.eventos.usuario.Usuario;

public record OrganizadorResumoResponse(Long id, String nome) {

	public static OrganizadorResumoResponse de(Usuario organizador) {
		return new OrganizadorResumoResponse(organizador.getId(), organizador.getNome());
	}

}
