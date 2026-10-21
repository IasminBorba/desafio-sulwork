package br.com.sulwork.eventos.evento.dto;

import java.time.Clock;
import java.time.Instant;

import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.StatusEvento;
import br.com.sulwork.eventos.evento.TipoEvento;

public record EventoDetalheResponse(
		Long id,
		String nome,
		String descricao,
		TipoEvento tipo,
		StatusEvento status,
		Instant dataInicio,
		Instant dataFim,
		Instant prazoAlteracao,
		OrganizadorResumoResponse organizador,
		boolean aceitaAlteracoes,
		boolean souOrganizador,
		boolean estouParticipando) {

	public static EventoDetalheResponse de(Evento evento, Long usuarioId, boolean estouParticipando, Clock clock) {
		boolean souOrganizador = evento.ehOrganizadoPor(usuarioId);
		return new EventoDetalheResponse(
				evento.getId(),
				evento.getNome(),
				evento.getDescricao(),
				evento.getTipo(),
				evento.getStatus(),
				evento.getDataInicio(),
				evento.getDataFim(),
				evento.getPrazoAlteracao(),
				OrganizadorResumoResponse.de(evento.getOrganizador()),
				evento.aceitaAlteracoes(clock),
				souOrganizador,
				estouParticipando);
	}

}
