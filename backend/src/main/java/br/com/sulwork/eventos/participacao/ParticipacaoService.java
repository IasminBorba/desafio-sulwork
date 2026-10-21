package br.com.sulwork.eventos.participacao;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.participacao.dto.ParticipacaoResponse;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.PermissaoNegadaException;
import br.com.sulwork.eventos.shared.exception.RecursoNaoEncontradoException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@Service
public class ParticipacaoService {

	private final ParticipacaoRepository participacaoRepository;
	private final EventoAcesso eventoAcesso;
	private final UsuarioAutenticado usuarioAutenticado;
	private final Clock clock;

	public ParticipacaoService(ParticipacaoRepository participacaoRepository, EventoAcesso eventoAcesso,
			UsuarioAutenticado usuarioAutenticado, Clock clock) {
		this.participacaoRepository = participacaoRepository;
		this.eventoAcesso = eventoAcesso;
		this.usuarioAutenticado = usuarioAutenticado;
		this.clock = clock;
	}

	@Transactional
	public List<ParticipacaoResponse> listar(Long eventoId) {
		Evento evento = eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		return participacaoRepository.findByEvento_IdOrderByUsuario_NomeAsc(evento.getId()).stream()
				.map(ParticipacaoResponse::de)
				.toList();
	}

	@Transactional
	public ParticipacaoResponse participar(Long eventoId) {
		Evento evento = eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		garantirAceitaAlteracoes(evento);
		Usuario usuario = usuarioAutenticado.get();
		if (participacaoRepository.existsByEvento_IdAndUsuario_Id(eventoId, usuario.getId())) {
			throw new ConflitoException("Você já está participando deste evento.");
		}
		Participacao participacao = participacaoRepository.save(Participacao.de(usuario, evento));
		return ParticipacaoResponse.de(participacao);
	}

	@Transactional
	public void cancelar(Long eventoId, Long participacaoId) {
		Evento evento = eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		garantirAceitaAlteracoes(evento);
		Participacao participacao = participacaoRepository.findByIdAndEvento_Id(participacaoId, eventoId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Participação", participacaoId));
		Long usuarioId = usuarioAutenticado.id();
		boolean dono = participacao.getUsuario().getId().equals(usuarioId);
		if (!dono && !evento.ehOrganizadoPor(usuarioId)) {
			throw new PermissaoNegadaException("Só é possível cancelar a própria participação.");
		}
		participacaoRepository.delete(participacao);
	}

	private void garantirAceitaAlteracoes(Evento evento) {
		if (!evento.aceitaAlteracoes(clock)) {
			throw new ConflitoException("O prazo para alterações neste evento já encerrou.");
		}
	}

}
