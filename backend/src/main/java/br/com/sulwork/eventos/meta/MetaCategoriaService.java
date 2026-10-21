package br.com.sulwork.eventos.meta;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.catalogo.CategoriaRepository;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.meta.dto.MetaRequest;
import br.com.sulwork.eventos.meta.dto.MetaResponse;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.RecursoNaoEncontradoException;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@Service
public class MetaCategoriaService {

	private final MetaCategoriaRepository metaCategoriaRepository;
	private final CategoriaRepository categoriaRepository;
	private final ItemRepository itemRepository;
	private final EventoAcesso eventoAcesso;
	private final UsuarioAutenticado usuarioAutenticado;

	public MetaCategoriaService(MetaCategoriaRepository metaCategoriaRepository,
			CategoriaRepository categoriaRepository, ItemRepository itemRepository, EventoAcesso eventoAcesso,
			UsuarioAutenticado usuarioAutenticado) {
		this.metaCategoriaRepository = metaCategoriaRepository;
		this.categoriaRepository = categoriaRepository;
		this.itemRepository = itemRepository;
		this.eventoAcesso = eventoAcesso;
		this.usuarioAutenticado = usuarioAutenticado;
	}

	@Transactional
	public List<MetaResponse> listar(Long eventoId) {
		Evento evento = eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		return metaCategoriaRepository.findByEvento_IdOrderByCategoria_NomeAsc(evento.getId()).stream()
				.map(MetaResponse::de)
				.toList();
	}

	@Transactional
	public MetaResponse criar(Long eventoId, MetaRequest request) {
		Evento evento = eventoAcesso.gerenciavel(eventoId, usuarioAutenticado.id());
		Categoria categoria = categoria(request.categoriaId());
		garantirItemNaCategoria(eventoId, categoria.getId());
		if (metaCategoriaRepository.existsByEvento_IdAndCategoria_Id(eventoId, categoria.getId())) {
			throw new ConflitoException("Já existe uma meta para essa categoria neste evento.");
		}
		MetaCategoria meta = MetaCategoria.nova(evento, categoria, request.quantidadeMeta());
		return MetaResponse.de(metaCategoriaRepository.save(meta));
	}

	@Transactional
	public MetaResponse atualizar(Long eventoId, Long metaId, MetaRequest request) {
		eventoAcesso.gerenciavel(eventoId, usuarioAutenticado.id());
		MetaCategoria meta = carregar(eventoId, metaId);
		Categoria categoria = categoria(request.categoriaId());
		if (!meta.getCategoria().getId().equals(categoria.getId())) {
			garantirItemNaCategoria(eventoId, categoria.getId());
			if (metaCategoriaRepository.existsByEvento_IdAndCategoria_Id(eventoId, categoria.getId())) {
				throw new ConflitoException("Já existe uma meta para essa categoria neste evento.");
			}
			meta.setCategoria(categoria);
		}
		meta.setQuantidadeMeta(request.quantidadeMeta());
		return MetaResponse.de(meta);
	}

	@Transactional
	public void excluir(Long eventoId, Long metaId) {
		eventoAcesso.gerenciavel(eventoId, usuarioAutenticado.id());
		MetaCategoria meta = carregar(eventoId, metaId);
		metaCategoriaRepository.delete(meta);
	}

	private MetaCategoria carregar(Long eventoId, Long metaId) {
		return metaCategoriaRepository.findByIdAndEvento_Id(metaId, eventoId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Meta", metaId));
	}

	private Categoria categoria(Long categoriaId) {
		return categoriaRepository.findById(categoriaId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Categoria", categoriaId));
	}

	private void garantirItemNaCategoria(Long eventoId, Long categoriaId) {
		if (!itemRepository.existsByEvento_IdAndCategoria_Id(eventoId, categoriaId)) {
			throw new RegraNegocioException(
					"Só é possível definir meta para uma categoria que já tenha item no evento.");
		}
	}

}
