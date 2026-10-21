package br.com.sulwork.eventos.evento;

import org.springframework.stereotype.Component;

import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.PermissaoNegadaException;
import br.com.sulwork.eventos.shared.exception.RecursoNaoEncontradoException;

@Component
public class EventoAcesso {

	private final EventoRepository eventoRepository;
	private final EncerramentoPorPrazoService encerramentoPorPrazoService;

	public EventoAcesso(EventoRepository eventoRepository, EncerramentoPorPrazoService encerramentoPorPrazoService) {
		this.eventoRepository = eventoRepository;
		this.encerramentoPorPrazoService = encerramentoPorPrazoService;
	}

	public Evento visivel(Long eventoId, Long usuarioId) {
		encerramentoPorPrazoService.encerrarVencidos();
		Evento evento = eventoRepository.findComOrganizadorById(eventoId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Evento", eventoId));
		if (evento.getStatus() == StatusEvento.RASCUNHO && !evento.ehOrganizadoPor(usuarioId)) {
			throw RecursoNaoEncontradoException.de("Evento", eventoId);
		}
		return evento;
	}

	public Evento comoOrganizador(Long eventoId, Long usuarioId) {
		Evento evento = visivel(eventoId, usuarioId);
		if (!evento.ehOrganizadoPor(usuarioId)) {
			throw new PermissaoNegadaException("Apenas o organizador pode alterar o evento.");
		}
		return evento;
	}

	public Evento gerenciavel(Long eventoId, Long usuarioId) {
		Evento evento = comoOrganizador(eventoId, usuarioId);
		if (evento.getStatus() != StatusEvento.RASCUNHO && evento.getStatus() != StatusEvento.ABERTO) {
			throw new ConflitoException("Itens e metas só podem ser alterados com o evento em rascunho ou aberto.");
		}
		return evento;
	}

}
