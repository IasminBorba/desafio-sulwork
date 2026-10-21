package br.com.sulwork.eventos.participacao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.evento.TipoEvento;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class ParticipacaoServiceTest {

	private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");
	private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

	@Mock
	private ParticipacaoRepository participacaoRepository;

	@Mock
	private EventoAcesso eventoAcesso;

	@Mock
	private UsuarioAutenticado usuarioAutenticado;

	@Test
	void naoPermiteParticipacaoDuplicada() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(usuarioAutenticado.get()).thenReturn(ana);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);
		when(participacaoRepository.existsByEvento_IdAndUsuario_Id(10L, 1L)).thenReturn(true);

		ParticipacaoService service = new ParticipacaoService(participacaoRepository, eventoAcesso, usuarioAutenticado,
				RELOGIO);

		assertThatThrownBy(() -> service.participar(10L))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("já está participando");
		verify(participacaoRepository, never()).save(any());
	}

	@Test
	void naoCancelaDepoisDoPrazo() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Clock depoisDoPrazo = Clock.fixed(AGORA.plus(Duration.ofDays(1)).plusSeconds(1), ZoneOffset.UTC);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);

		ParticipacaoService service = new ParticipacaoService(participacaoRepository, eventoAcesso, usuarioAutenticado,
				depoisDoPrazo);

		assertThatThrownBy(() -> service.cancelar(10L, 3L))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("prazo");
		verify(participacaoRepository, never()).delete(any());
	}

	@Test
	void naoParticipaDeRascunho() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = Evento.novo(
				"Churrasco",
				"Equipe",
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(2)),
				AGORA.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
				AGORA.plus(Duration.ofDays(1)),
				ana);
		evento.setId(10L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);

		ParticipacaoService service = new ParticipacaoService(participacaoRepository, eventoAcesso, usuarioAutenticado,
				RELOGIO);

		assertThatThrownBy(() -> service.participar(10L))
				.isInstanceOf(ConflitoException.class);
	}

	private static Evento eventoAberto(Usuario organizador) {
		Evento evento = Evento.novo(
				"Churrasco",
				"Equipe",
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(2)),
				AGORA.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
				AGORA.plus(Duration.ofDays(1)),
				organizador);
		evento.setId(10L);
		evento.publicar(RELOGIO, true);
		return evento;
	}

	private static Usuario usuario(Long id, String nome) {
		Usuario usuario = Usuario.novo(nome, "11144477735", nome.toLowerCase() + "@example.com", "hash");
		usuario.setId(id);
		return usuario;
	}

}
