package br.com.sulwork.eventos.item;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sulwork.eventos.catalogo.AlergenoRepository;
import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.catalogo.CategoriaRepository;
import br.com.sulwork.eventos.contribuicao.ContribuicaoRepository;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.evento.TipoEvento;
import br.com.sulwork.eventos.item.dto.ItemRequest;
import br.com.sulwork.eventos.meta.MetaCategoriaRepository;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	@Mock
	private AlergenoRepository alergenoRepository;

	@Mock
	private ContribuicaoRepository contribuicaoRepository;

	@Mock
	private MetaCategoriaRepository metaCategoriaRepository;

	@Mock
	private EventoAcesso eventoAcesso;

	@Mock
	private UsuarioAutenticado usuarioAutenticado;

	private ItemService itemService;

	@BeforeEach
	void setUp() {
		itemService = new ItemService(itemRepository, categoriaRepository, alergenoRepository, contribuicaoRepository,
				metaCategoriaRepository, eventoAcesso, usuarioAutenticado);
	}

	@Test
	void naoExcluiUltimoItemDaCategoriaComMeta() {
		Evento evento = eventoDa();
		Categoria bebidas = categoria(1L, "Bebidas");
		Item item = Item.novo(evento, bebidas, "Refrigerante", 10, true);
		item.setId(5L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.gerenciavel(10L, 1L)).thenReturn(evento);
		when(itemRepository.findByIdAndEvento_Id(5L, 10L)).thenReturn(Optional.of(item));
		when(contribuicaoRepository.existsByItem_Id(5L)).thenReturn(false);
		when(metaCategoriaRepository.existsByEvento_IdAndCategoria_Id(10L, 1L)).thenReturn(true);
		when(itemRepository.countByEvento_IdAndCategoria_Id(10L, 1L)).thenReturn(1L);

		assertThatThrownBy(() -> itemService.excluir(10L, 5L))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("último item");
		verify(itemRepository, never()).delete(item);
	}

	@Test
	void naoReduzQuantidadeAbaixoDoContribuido() {
		Evento evento = eventoDa();
		Categoria bebidas = categoria(1L, "Bebidas");
		Item item = Item.novo(evento, bebidas, "Refrigerante", 10, true);
		item.setId(5L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.gerenciavel(10L, 1L)).thenReturn(evento);
		when(itemRepository.findByIdAndEvento_Id(5L, 10L)).thenReturn(Optional.of(item));
		when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));
		when(contribuicaoRepository.somaQuantidadePorItem(5L)).thenReturn(6);

		ItemRequest pedido = new ItemRequest("Refrigerante", 1L, 4, true, List.of());

		assertThatThrownBy(() -> itemService.atualizar(10L, 5L, pedido))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("já contribuído");
	}

	@Test
	void naoExcluiItemComContribuicao() {
		Evento evento = eventoDa();
		Categoria bebidas = categoria(1L, "Bebidas");
		Item item = Item.novo(evento, bebidas, "Refrigerante", 10, true);
		item.setId(5L);
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.gerenciavel(10L, 1L)).thenReturn(evento);
		when(itemRepository.findByIdAndEvento_Id(5L, 10L)).thenReturn(Optional.of(item));
		when(contribuicaoRepository.existsByItem_Id(5L)).thenReturn(true);

		assertThatThrownBy(() -> itemService.excluir(10L, 5L))
				.isInstanceOf(ConflitoException.class)
				.hasMessageContaining("contribuições");
	}

	private static Evento eventoDa() {
		Usuario ana = Usuario.novo("Ana", "11144477735", "ana@example.com", "hash");
		ana.setId(1L);
		Evento evento = Evento.novo(
				"Churrasco",
				"Equipe",
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(2)),
				AGORA.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
				AGORA.plus(Duration.ofDays(1)),
				ana);
		evento.setId(10L);
		return evento;
	}

	private static Categoria categoria(Long id, String nome) {
		Categoria categoria = mock(Categoria.class);
		lenient().when(categoria.getId()).thenReturn(id);
		lenient().when(categoria.getNome()).thenReturn(nome);
		return categoria;
	}

}
