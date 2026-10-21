package br.com.sulwork.eventos.item;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.catalogo.Alergeno;
import br.com.sulwork.eventos.catalogo.AlergenoRepository;
import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.catalogo.CategoriaRepository;
import br.com.sulwork.eventos.contribuicao.ContribuicaoRepository;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.item.dto.ItemRequest;
import br.com.sulwork.eventos.item.dto.ItemResponse;
import br.com.sulwork.eventos.meta.MetaCategoriaRepository;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.RecursoNaoEncontradoException;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@Service
public class ItemService {

	private final ItemRepository itemRepository;
	private final CategoriaRepository categoriaRepository;
	private final AlergenoRepository alergenoRepository;
	private final ContribuicaoRepository contribuicaoRepository;
	private final MetaCategoriaRepository metaCategoriaRepository;
	private final EventoAcesso eventoAcesso;
	private final UsuarioAutenticado usuarioAutenticado;

	public ItemService(ItemRepository itemRepository, CategoriaRepository categoriaRepository,
			AlergenoRepository alergenoRepository, ContribuicaoRepository contribuicaoRepository,
			MetaCategoriaRepository metaCategoriaRepository, EventoAcesso eventoAcesso,
			UsuarioAutenticado usuarioAutenticado) {
		this.itemRepository = itemRepository;
		this.categoriaRepository = categoriaRepository;
		this.alergenoRepository = alergenoRepository;
		this.contribuicaoRepository = contribuicaoRepository;
		this.metaCategoriaRepository = metaCategoriaRepository;
		this.eventoAcesso = eventoAcesso;
		this.usuarioAutenticado = usuarioAutenticado;
	}

	@Transactional
	public List<ItemResponse> listar(Long eventoId) {
		Evento evento = eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		return itemRepository.findByEvento_IdOrderByNomeAsc(evento.getId()).stream()
				.map(ItemResponse::de)
				.toList();
	}

	@Transactional
	public ItemResponse criar(Long eventoId, ItemRequest request) {
		Evento evento = eventoAcesso.gerenciavel(eventoId, usuarioAutenticado.id());
		Item item = Item.novo(
				evento,
				categoria(request.categoriaId()),
				request.nome().strip(),
				request.quantidadeNecessaria(),
				request.obrigatorio());
		item.substituirAlergenos(alergenos(request.alergenoIds()));
		return ItemResponse.de(itemRepository.save(item));
	}

	@Transactional
	public ItemResponse atualizar(Long eventoId, Long itemId, ItemRequest request) {
		eventoAcesso.gerenciavel(eventoId, usuarioAutenticado.id());
		Item item = carregar(eventoId, itemId);
		Categoria novaCategoria = categoria(request.categoriaId());
		if (!item.getCategoria().getId().equals(novaCategoria.getId())) {
			garantirNaoEUltimoComMeta(eventoId, item.getCategoria().getId());
		}
		int contribuido = contribuicaoRepository.somaQuantidadePorItem(itemId);
		if (request.quantidadeNecessaria() < contribuido) {
			throw new ConflitoException(
					"A quantidade necessária não pode ser menor do que o já contribuído (%d)."
							.formatted(contribuido));
		}
		item.atualizar(novaCategoria, request.nome().strip(), request.quantidadeNecessaria(), request.obrigatorio());
		item.substituirAlergenos(alergenos(request.alergenoIds()));
		return ItemResponse.de(item);
	}

	@Transactional
	public void excluir(Long eventoId, Long itemId) {
		eventoAcesso.gerenciavel(eventoId, usuarioAutenticado.id());
		Item item = carregar(eventoId, itemId);
		if (contribuicaoRepository.existsByItem_Id(itemId)) {
			throw new ConflitoException("Não é possível excluir um item que já recebeu contribuições.");
		}
		garantirNaoEUltimoComMeta(eventoId, item.getCategoria().getId());
		itemRepository.delete(item);
	}

	private Item carregar(Long eventoId, Long itemId) {
		return itemRepository.findByIdAndEvento_Id(itemId, eventoId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Item", itemId));
	}

	private Categoria categoria(Long categoriaId) {
		return categoriaRepository.findById(categoriaId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Categoria", categoriaId));
	}

	private Set<Alergeno> alergenos(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Set.of();
		}
		List<Alergeno> encontrados = alergenoRepository.findAllById(ids);
		if (encontrados.size() != ids.stream().distinct().count()) {
			throw new RegraNegocioException("Um ou mais alérgenos informados não existem.");
		}
		return new LinkedHashSet<>(encontrados);
	}

	private void garantirNaoEUltimoComMeta(Long eventoId, Long categoriaId) {
		if (metaCategoriaRepository.existsByEvento_IdAndCategoria_Id(eventoId, categoriaId)
				&& itemRepository.countByEvento_IdAndCategoria_Id(eventoId, categoriaId) <= 1) {
			throw new ConflitoException(
					"Não é possível remover o último item da categoria enquanto houver uma meta.");
		}
	}

}
