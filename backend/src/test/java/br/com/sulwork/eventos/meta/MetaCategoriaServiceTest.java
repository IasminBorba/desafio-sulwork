package br.com.sulwork.eventos.meta;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.catalogo.CategoriaRepository;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.evento.TipoEvento;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.meta.dto.MetaRequest;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class MetaCategoriaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");

	@Mock
	private MetaCategoriaRepository metaCategoriaRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private EventoAcesso eventoAcesso;

	@Mock
	private UsuarioAutenticado usuarioAutenticado;

	private MetaCategoriaService metaCategoriaService;

	@BeforeEach
	void setUp() {
		metaCategoriaService = new MetaCategoriaService(metaCategoriaRepository, categoriaRepository, itemRepository,
				eventoAcesso, usuarioAutenticado);
	}

	@Test
	void naoCriaMetaSemItemDaCategoria() {
		Evento evento = eventoDa();
		Categoria bebidas = categoria(1L, "Bebidas");
		when(usuarioAutenticado.id()).thenReturn(1L);
		when(eventoAcesso.gerenciavel(10L, 1L)).thenReturn(evento);
		when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));
		when(itemRepository.existsByEvento_IdAndCategoria_Id(10L, 1L)).thenReturn(false);

		assertThatThrownBy(() -> metaCategoriaService.criar(10L, new MetaRequest(1L, 12)))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessageContaining("já tenha item");
		verify(metaCategoriaRepository, never()).save(any());
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
