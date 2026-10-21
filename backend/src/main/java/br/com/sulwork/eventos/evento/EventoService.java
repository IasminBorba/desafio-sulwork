package br.com.sulwork.eventos.evento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.evento.dto.EventoDetalheResponse;
import br.com.sulwork.eventos.evento.dto.EventoRequest;
import br.com.sulwork.eventos.evento.dto.EventoResumoResponse;
import br.com.sulwork.eventos.evento.dto.ReabrirRequest;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.participacao.ParticipacaoRepository;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@Service
public class EventoService {

	private final EventoRepository eventoRepository;
	private final ItemRepository itemRepository;
	private final ParticipacaoRepository participacaoRepository;
	private final EventoAcesso eventoAcesso;
	private final EncerramentoPorPrazoService encerramentoPorPrazoService;
	private final UsuarioAutenticado usuarioAutenticado;
	private final java.time.Clock clock;

	public EventoService(EventoRepository eventoRepository, ItemRepository itemRepository,
			ParticipacaoRepository participacaoRepository, EventoAcesso eventoAcesso,
			EncerramentoPorPrazoService encerramentoPorPrazoService, UsuarioAutenticado usuarioAutenticado,
			java.time.Clock clock) {
		this.eventoRepository = eventoRepository;
		this.itemRepository = itemRepository;
		this.participacaoRepository = participacaoRepository;
		this.eventoAcesso = eventoAcesso;
		this.encerramentoPorPrazoService = encerramentoPorPrazoService;
		this.usuarioAutenticado = usuarioAutenticado;
		this.clock = clock;
	}

	@Transactional
	public EventoDetalheResponse criar(EventoRequest request) {
		Usuario organizador = usuarioAutenticado.get();
		Evento evento = Evento.novo(
				request.nome().strip(),
				request.descricao().strip(),
				request.tipo(),
				request.dataInicio(),
				request.dataFim(),
				request.prazoAlteracao(),
				organizador);
		return detalhe(eventoRepository.save(evento), organizador.getId());
	}

	@Transactional
	public Page<EventoResumoResponse> listar(StatusEvento status, PapelEvento papel, Pageable pageable) {
		encerramentoPorPrazoService.encerrarVencidos();
		Long usuarioId = usuarioAutenticado.id();
		Page<Evento> pagina = switch (papel) {
			case ORGANIZADOR -> status == null
					? eventoRepository.findByOrganizador_Id(usuarioId, pageable)
					: eventoRepository.findByOrganizador_IdAndStatus(usuarioId, status, pageable);
			case PARTICIPANTE -> eventoRepository.findParticipando(usuarioId, status, pageable);
			case null -> eventoRepository.findVisiveisDoUsuario(usuarioId, status, pageable);
		};
		return pagina.map(evento -> EventoResumoResponse.de(evento, usuarioId));
	}

	@Transactional
	public EventoDetalheResponse buscar(Long id) {
		encerramentoPorPrazoService.encerrarVencidos();
		Long usuarioId = usuarioAutenticado.id();
		return detalhe(carregarVisivel(id, usuarioId), usuarioId);
	}

	@Transactional
	public EventoDetalheResponse atualizar(Long id, EventoRequest request) {
		encerramentoPorPrazoService.encerrarVencidos();
		Long usuarioId = usuarioAutenticado.id();
		Evento evento = carregarComoOrganizador(id, usuarioId);
		evento.atualizar(
				request.nome().strip(),
				request.descricao().strip(),
				request.tipo(),
				request.dataInicio(),
				request.dataFim(),
				request.prazoAlteracao());
		return detalhe(evento, usuarioId);
	}

	@Transactional
	public void excluir(Long id) {
		Long usuarioId = usuarioAutenticado.id();
		Evento evento = carregarComoOrganizador(id, usuarioId);
		evento.garantirExclusao();
		eventoRepository.delete(evento);
	}

	@Transactional
	public EventoDetalheResponse publicar(Long id) {
		return transitar(id, evento -> evento.publicar(clock, itemRepository.existsByEvento_Id(evento.getId())));
	}

	@Transactional
	public EventoDetalheResponse encerrar(Long id) {
		return transitar(id, Evento::encerrar);
	}

	@Transactional
	public EventoDetalheResponse reabrir(Long id, ReabrirRequest request) {
		return transitar(id, evento -> evento.reabrir(clock, request.prazoAlteracao(), request.dataInicio(),
				request.dataFim()));
	}

	@Transactional
	public EventoDetalheResponse concluir(Long id) {
		return transitar(id, evento -> evento.concluir(clock));
	}

	@Transactional
	public EventoDetalheResponse cancelar(Long id) {
		return transitar(id, Evento::cancelar);
	}

	private EventoDetalheResponse transitar(Long id, java.util.function.Consumer<Evento> acao) {
		encerramentoPorPrazoService.encerrarVencidos();
		Long usuarioId = usuarioAutenticado.id();
		Evento evento = carregarComoOrganizador(id, usuarioId);
		acao.accept(evento);
		return detalhe(evento, usuarioId);
	}

	private Evento carregarVisivel(Long id, Long usuarioId) {
		return eventoAcesso.visivel(id, usuarioId);
	}

	private Evento carregarComoOrganizador(Long id, Long usuarioId) {
		return eventoAcesso.comoOrganizador(id, usuarioId);
	}

	private EventoDetalheResponse detalhe(Evento evento, Long usuarioId) {
		boolean participando = participacaoRepository.existsByEvento_IdAndUsuario_Id(evento.getId(), usuarioId);
		return EventoDetalheResponse.de(evento, usuarioId, participando, clock);
	}

}
