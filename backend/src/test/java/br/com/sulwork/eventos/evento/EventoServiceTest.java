package br.com.sulwork.eventos.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sulwork.eventos.evento.dto.EventoRequest;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.participacao.ParticipacaoRepository;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.PermissaoNegadaException;
import br.com.sulwork.eventos.shared.exception.RecursoNaoEncontradoException;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

	private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");
	private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

	@Mock
	private EventoRepository eventoRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private ParticipacaoRepository participacaoRepository;

	@Mock
	private EncerramentoPorPrazoService encerramentoPorPrazoService;

	@Mock
	private UsuarioAutenticado usuarioAutenticado;

	@InjectMocks
	private EventoService eventoService;

	@BeforeEach
	void setUp() {
		eventoService = new EventoService(eventoRepository, itemRepository, participacaoRepository,
				encerramentoPorPrazoService, usuarioAutenticado, RELOGIO);
	}

	@Test
	void criarDefineOrganizadorERascunho() {
		Usuario ana = usuario(1L, "Ana");
		when(usuarioAutenticado.get()).thenReturn(ana);
		when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> {
			Evento evento = inv.getArgument(0);
			evento.setId(10L);
			return evento;
		});
		when(participacaoRepository.existsByEvento_IdAndUsuario_Id(10L, 1L)).thenReturn(false);

		var resposta = eventoService.criar(pedidoValido());

		assertThat(resposta.status()).isEqualTo(StatusEvento.RASCUNHO);
		assertThat(resposta.souOrganizador()).isTrue();
		assertThat(resposta.organizador().id()).isEqualTo(1L);
	}

	@Test
	void criarComPrazoDepoisDoInicioRetorna422() {
		when(usuarioAutenticado.get()).thenReturn(usuario(1L, "Ana"));

		EventoRequest invalido = new EventoRequest(
				"Churrasco",
				"Equipe",
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(1)),
				AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(4)),
				AGORA.plus(Duration.ofDays(2)));

		assertThatThrownBy(() -> eventoService.criar(invalido))
				.isInstanceOf(RegraNegocioException.class);
	}

	@Test
	void rascunhoDeOutraPessoaPareceInexistente() {
		Evento evento = eventoDa(usuario(1L, "Ana"));
		evento.setId(10L);
		when(usuarioAutenticado.id()).thenReturn(2L);
		when(eventoRepository.findComOrganizadorById(10L)).thenReturn(java.util.Optional.of(evento));

		assertThatThrownBy(() -> eventoService.buscar(10L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
		verify(encerramentoPorPrazoService).encerrarVencidos();
	}

	@Test
	void naoOrganizadorNaoEditaEventoAberto() {
		Evento evento = eventoDa(usuario(1L, "Ana"));
		evento.setId(10L);
		evento.publicar(RELOGIO, true);
		when(usuarioAutenticado.id()).thenReturn(2L);
		when(eventoRepository.findComOrganizadorById(10L)).thenReturn(java.util.Optional.of(evento));

		assertThatThrownBy(() -> eventoService.atualizar(10L, pedidoValido()))
				.isInstanceOf(PermissaoNegadaException.class);
	}

	@Test
	void publicarSemItemRetorna422() {
		Evento evento = eventoDa(usuario(1L, "Ana"));
		evento.setId(10L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoRepository.findComOrganizadorById(10L)).thenReturn(java.util.Optional.of(evento));
		when(itemRepository.existsByEvento_Id(10L)).thenReturn(false);

		assertThatThrownBy(() -> eventoService.publicar(10L))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessageContaining("pelo menos um item");
	}

	@Test
	void excluirAbertoRetorna409() {
		Evento evento = eventoDa(usuario(1L, "Ana"));
		evento.setId(10L);
		evento.publicar(RELOGIO, true);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoRepository.findComOrganizadorById(10L)).thenReturn(java.util.Optional.of(evento));

		assertThatThrownBy(() -> eventoService.excluir(10L))
				.isInstanceOf(ConflitoException.class);
	}

	private static EventoRequest pedidoValido() {
		return new EventoRequest(
				"Churrasco",
				"Equipe",
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(2)),
				AGORA.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
				AGORA.plus(Duration.ofDays(1)));
	}

	private static Evento eventoDa(Usuario organizador) {
		return Evento.novo(
				"Churrasco",
				"Equipe",
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(2)),
				AGORA.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
				AGORA.plus(Duration.ofDays(1)),
				organizador);
	}

	private static Usuario usuario(Long id, String nome) {
		Usuario usuario = Usuario.novo(nome, "11144477735", nome.toLowerCase() + "@example.com", "hash");
		usuario.setId(id);
		return usuario;
	}

}
