package br.com.sulwork.eventos.cobertura;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.cobertura.dto.CoberturaResponse;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.meta.MetaCategoriaRepository;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@Service
public class CoberturaService {

	private final ItemRepository itemRepository;
	private final MetaCategoriaRepository metaCategoriaRepository;
	private final CalculadoraCobertura calculadoraCobertura;
	private final EventoAcesso eventoAcesso;
	private final UsuarioAutenticado usuarioAutenticado;

	public CoberturaService(ItemRepository itemRepository, MetaCategoriaRepository metaCategoriaRepository,
			CalculadoraCobertura calculadoraCobertura, EventoAcesso eventoAcesso,
			UsuarioAutenticado usuarioAutenticado) {
		this.itemRepository = itemRepository;
		this.metaCategoriaRepository = metaCategoriaRepository;
		this.calculadoraCobertura = calculadoraCobertura;
		this.eventoAcesso = eventoAcesso;
		this.usuarioAutenticado = usuarioAutenticado;
	}

	@Transactional
	public CoberturaResponse calcular(Long eventoId) {
		eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		return calcularSemAutorizacao(eventoId);
	}

	public CoberturaResponse calcularSemAutorizacao(Long eventoId) {
		List<ItemCoberturaDados> itens = itemRepository.somarContribuicoesDoEvento(eventoId).stream()
				.map(ItemCoberturaDados::de)
				.toList();
		List<MetaCoberturaDados> metas = metaCategoriaRepository.listarDoEvento(eventoId).stream()
				.map(MetaCoberturaDados::de)
				.toList();
		return calculadoraCobertura.calcular(itens, metas);
	}

}
