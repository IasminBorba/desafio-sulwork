package br.com.sulwork.eventos.evento.dto;

import java.time.Instant;

import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.StatusEvento;
import br.com.sulwork.eventos.evento.TipoEvento;

public record EventoResumoResponse(
		Long id,
		String nome,
		TipoEvento tipo,
		StatusEvento status,
		Instant dataInicio,
		Instant dataFim,
		Instant prazoAlteracao,
		boolean souOrganizador) {

	public static EventoResumoResponse de(Evento evento, Long usuarioId) {
		return new EventoResumoResponse(
				evento.getId(),
				evento.getNome(),
				evento.getTipo(),
				evento.getStatus(),
				evento.getDataInicio(),
				evento.getDataFim(),
				evento.getPrazoAlteracao(),
				evento.ehOrganizadoPor(usuarioId));
	}

}
