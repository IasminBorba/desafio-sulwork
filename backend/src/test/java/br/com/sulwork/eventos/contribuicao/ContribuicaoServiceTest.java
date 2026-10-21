package br.com.sulwork.eventos.contribuicao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.contribuicao.dto.ContribuicaoRequest;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.evento.TipoEvento;
import br.com.sulwork.eventos.item.Item;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.participacao.Participacao;
import br.com.sulwork.eventos.participacao.ParticipacaoRepository;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.PermissaoNegadaException;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class ContribuicaoServiceTest {

	private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");
	private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

	@Mock
	private ContribuicaoRepository contribuicaoRepository;

	@Mock
	private ParticipacaoRepository participacaoRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private EventoAcesso eventoAcesso;

	@Mock
	private UsuarioAutenticado usuarioAutenticado;

	@Mock
	private Categoria categoria;

	private ContribuicaoService service;

	@BeforeEach
	void setUp() {
		service = new ContribuicaoService(contribuicaoRepository, participacaoRepository, itemRepository, eventoAcesso,
				usuarioAutenticado, RELOGIO);
	}

	@Test
	void recusaQuantidadeAcimaDoFaltante() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Item item = itemDo(evento, 5L, 10);
		cenarioAberto(evento, ana, item);
		when(contribuicaoRepository.existsByParticipacao_IdAndItem_Id(20L, 5L)).thenReturn(false);
		when(contribuicaoRepository.somaQuantidadePorItem(5L)).thenReturn(8);

		assertThatThrownBy(() -> service.criar(10L, new ContribuicaoRequest(5L, 3)))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("Restam 2");
		verify(contribuicaoRepository, never()).save(any());
	}

	@Test
	void recusaContribuicaoAposOPrazo() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Clock depoisDoPrazo = Clock.fixed(AGORA.plus(Duration.ofDays(1)).plusSeconds(1), ZoneOffset.UTC);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);
		service = new ContribuicaoService(contribuicaoRepository, participacaoRepository, itemRepository, eventoAcesso,
				usuarioAutenticado, depoisDoPrazo);

		assertThatThrownBy(() -> service.criar(10L, new ContribuicaoRequest(5L, 2)))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("prazo");
		verify(itemRepository, never()).findByIdForUpdate(any());
	}

	@Test
	void recusaQuemAindaNaoParticipa() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);
		when(participacaoRepository.findByEvento_IdAndUsuario_Id(10L, 1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.criar(10L, new ContribuicaoRequest(5L, 2)))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessageContaining("Participe do evento primeiro");
	}

	@Test
	void recusaItemDeOutroEvento() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Evento outro = eventoAberto(ana);
		outro.setId(99L);
		Item item = itemDo(outro, 5L, 10);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);
		when(participacaoRepository.findByEvento_IdAndUsuario_Id(10L, 1L))
				.thenReturn(Optional.of(participacao(20L, ana, evento)));
		when(itemRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(item));

		assertThatThrownBy(() -> service.criar(10L, new ContribuicaoRequest(5L, 2)))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessageContaining("não pertence");
	}

	@Test
	void edicaoDescontaAPropriaQuantidade() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Item item = itemDo(evento, 5L, 10);
		Participacao participacao = participacao(20L, ana, evento);
		Contribuicao existente = Contribuicao.nova(participacao, item, 4);
		existente.setId(30L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);
		when(contribuicaoRepository.findByIdAndParticipacao_Evento_Id(30L, 10L)).thenReturn(Optional.of(existente));
		when(itemRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(item));
		when(contribuicaoRepository.somaQuantidadePorItemExcluindo(5L, 30L)).thenReturn(6);

		var resposta = service.atualizar(10L, 30L, new ContribuicaoRequest(5L, 4));

		assertThat(resposta.quantidade()).isEqualTo(4);
		assertThat(existente.getQuantidade()).isEqualTo(4);
	}

	@Test
	void edicaoAcimaDoFaltanteConsiderandoAsOutras() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Item item = itemDo(evento, 5L, 10);
		Participacao participacao = participacao(20L, ana, evento);
		Contribuicao existente = Contribuicao.nova(participacao, item, 4);
		existente.setId(30L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);
		when(contribuicaoRepository.findByIdAndParticipacao_Evento_Id(30L, 10L)).thenReturn(Optional.of(existente));
		when(itemRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(item));
		when(contribuicaoRepository.somaQuantidadePorItemExcluindo(5L, 30L)).thenReturn(8);

		assertThatThrownBy(() -> service.atualizar(10L, 30L, new ContribuicaoRequest(5L, 3)))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("Restam 2");
	}

	@Test
	void recusaSegundaContribuicaoNoMesmoItem() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Item item = itemDo(evento, 5L, 10);
		cenarioAberto(evento, ana, item);
		when(contribuicaoRepository.existsByParticipacao_IdAndItem_Id(20L, 5L)).thenReturn(true);

		assertThatThrownBy(() -> service.criar(10L, new ContribuicaoRequest(5L, 2)))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("já contribuiu");
	}

	@Test
	void naoPermiteEditarContribuicaoDeOutraPessoa() {
		Usuario ana = usuario(1L, "Ana");
		Usuario bia = usuario(2L, "Bia");
		Evento evento = eventoAberto(ana);
		Item item = itemDo(evento, 5L, 10);
		Contribuicao daBia = Contribuicao.nova(participacao(21L, bia, evento), item, 3);
		daBia.setId(30L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.visivel(10L, 1L)).thenReturn(evento);
		when(contribuicaoRepository.findByIdAndParticipacao_Evento_Id(30L, 10L)).thenReturn(Optional.of(daBia));

		assertThatThrownBy(() -> service.atualizar(10L, 30L, new ContribuicaoRequest(5L, 2)))
				.isInstanceOf(PermissaoNegadaException.class);
	}

	@Test
	void criaQuandoCabeNoFaltante() {
		Usuario ana = usuario(1L, "Ana");
		Evento evento = eventoAberto(ana);
		Item item = itemDo(evento, 5L, 10);
		cenarioAberto(evento, ana, item);
		when(contribuicaoRepository.existsByParticipacao_IdAndItem_Id(20L, 5L)).thenReturn(false);
		when(contribuicaoRepository.somaQuantidadePorItem(5L)).thenReturn(7);
		when(contribuicaoRepository.save(any(Contribuicao.class))).thenAnswer(inv -> {
			Contribuicao salva = inv.getArgument(0);
			salva.setId(40L);
			return salva;
		});

		var resposta = service.criar(10L, new ContribuicaoRequest(5L, 3));

		assertThat(resposta.quantidade()).isEqualTo(3);
		ArgumentCaptor<Contribuicao> captor = ArgumentCaptor.forClass(Contribuicao.class);
		verify(contribuicaoRepository).save(captor.capture());
		assertThat(captor.getValue().getQuantidade()).isEqualTo(3);
	}

	private void cenarioAberto(Evento evento, Usuario usuario, Item item) {
		when(usuarioAutenticado.id()).thenReturn(usuario.getId());
		when(eventoAcesso.visivel(10L, usuario.getId())).thenReturn(evento);
		when(participacaoRepository.findByEvento_IdAndUsuario_Id(10L, usuario.getId()))
				.thenReturn(Optional.of(participacao(20L, usuario, evento)));
		when(itemRepository.findByIdForUpdate(item.getId())).thenReturn(Optional.of(item));
	}

	private Evento eventoAberto(Usuario organizador) {
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

	private Item itemDo(Evento evento, Long id, int necessaria) {
		Item item = Item.novo(evento, categoria, "Refrigerante", necessaria, true);
		item.setId(id);
		return item;
	}

	private static Participacao participacao(Long id, Usuario usuario, Evento evento) {
		Participacao participacao = Participacao.de(usuario, evento);
		participacao.setId(id);
		return participacao;
	}

	private static Usuario usuario(Long id, String nome) {
		Usuario usuario = Usuario.novo(nome, "11144477735", nome.toLowerCase() + "@example.com", "hash");
		usuario.setId(id);
		return usuario;
	}

}
