package br.com.sulwork.eventos.painel.dto;

import java.util.List;

import br.com.sulwork.eventos.cobertura.dto.CoberturaResponse;
import br.com.sulwork.eventos.evento.dto.EventoDetalheResponse;
import br.com.sulwork.eventos.restricao.dto.RestricoesEventoResponse;

public record PainelResponse(
		EventoDetalheResponse evento,
		CoberturaResponse cobertura,
		RestricoesEventoResponse restricoes,
		List<PainelParticipanteResponse> participantes) {
}
