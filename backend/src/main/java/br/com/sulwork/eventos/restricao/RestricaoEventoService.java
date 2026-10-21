package br.com.sulwork.eventos.restricao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.catalogo.Alergeno;
import br.com.sulwork.eventos.catalogo.RestricaoAlimentar;
import br.com.sulwork.eventos.catalogo.RestricaoAlimentarRepository;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.item.Item;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.participacao.Participacao;
import br.com.sulwork.eventos.participacao.ParticipacaoRepository;
import br.com.sulwork.eventos.restricao.dto.ItemRestricaoResponse;
import br.com.sulwork.eventos.restricao.dto.ParticipanteAfetadoResponse;
import br.com.sulwork.eventos.restricao.dto.RestricaoResumoEventoResponse;
import br.com.sulwork.eventos.restricao.dto.RestricaoSemItemResponse;
import br.com.sulwork.eventos.restricao.dto.RestricoesEventoResponse;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@Service
public class RestricaoEventoService {

	private final ParticipacaoRepository participacaoRepository;
	private final ItemRepository itemRepository;
	private final RestricaoAlimentarRepository restricaoAlimentarRepository;
	private final EventoAcesso eventoAcesso;
	private final UsuarioAutenticado usuarioAutenticado;

	public RestricaoEventoService(ParticipacaoRepository participacaoRepository, ItemRepository itemRepository,
			RestricaoAlimentarRepository restricaoAlimentarRepository, EventoAcesso eventoAcesso,
			UsuarioAutenticado usuarioAutenticado) {
		this.participacaoRepository = participacaoRepository;
		this.itemRepository = itemRepository;
		this.restricaoAlimentarRepository = restricaoAlimentarRepository;
		this.eventoAcesso = eventoAcesso;
		this.usuarioAutenticado = usuarioAutenticado;
	}

	@Transactional
	public RestricoesEventoResponse analisar(Long eventoId) {
		eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		return analisarSemAutorizacao(eventoId);
	}

	public RestricoesEventoResponse analisarSemAutorizacao(Long eventoId) {
		Map<Long, Set<Long>> alergenosPorRestricao = restricaoAlimentarRepository.findAllByOrderByNomeAsc().stream()
				.collect(Collectors.toMap(RestricaoAlimentar::getId, restricao -> ids(restricao.getAlergenos())));
		List<Participacao> participacoes = participacaoRepository.findByEvento_IdOrderByUsuario_NomeAsc(eventoId);
		List<Item> itens = itemRepository.findByEvento_IdOrderByNomeAsc(eventoId);

		Map<Long, ContagemRestricao> resumo = new LinkedHashMap<>();
		for (Participacao participacao : participacoes) {
			for (RestricaoAlimentar restricao : participacao.getUsuario().getRestricoes()) {
				resumo.computeIfAbsent(restricao.getId(),
						id -> new ContagemRestricao(id, restricao.getNome())).quantidade++;
			}
		}

		List<ItemRestricaoResponse> porItem = itens.stream()
				.map(item -> conflitosDoItem(item, participacoes, alergenosPorRestricao))
				.toList();

		List<RestricaoSemItemResponse> semItem = new ArrayList<>();
		for (ContagemRestricao restricao : resumo.values()) {
			Set<Long> alergenos = alergenosPorRestricao.getOrDefault(restricao.id, Set.of());
			boolean algumCompativel = itens.stream()
					.anyMatch(item -> !conflita(ids(item.getAlergenos()), alergenos));
			if (!algumCompativel) {
				semItem.add(new RestricaoSemItemResponse(restricao.id, restricao.nome, restricao.quantidade));
			}
		}

		List<RestricaoResumoEventoResponse> resumoResposta = resumo.values().stream()
				.map(item -> new RestricaoResumoEventoResponse(item.id, item.nome, item.quantidade))
				.toList();
		return new RestricoesEventoResponse(resumoResposta, porItem, semItem);
	}

	private ItemRestricaoResponse conflitosDoItem(Item item, List<Participacao> participacoes,
			Map<Long, Set<Long>> alergenosPorRestricao) {
		Set<Long> alergenosItem = ids(item.getAlergenos());
		List<ParticipanteAfetadoResponse> afetados = new ArrayList<>();
		for (Participacao participacao : participacoes) {
			Usuario usuario = participacao.getUsuario();
			boolean conflita = usuario.getRestricoes().stream()
					.map(restricao -> alergenosPorRestricao.getOrDefault(restricao.getId(), Set.of()))
					.anyMatch(alergenos -> conflita(alergenosItem, alergenos));
			if (conflita) {
				afetados.add(new ParticipanteAfetadoResponse(usuario.getId(), usuario.getNome()));
			}
		}
		return new ItemRestricaoResponse(item.getId(), item.getNome(), afetados.size(), afetados);
	}

	private static boolean conflita(Set<Long> alergenosItem, Set<Long> alergenosRestricao) {
		return alergenosItem.stream().anyMatch(alergenosRestricao::contains);
	}

	private static Set<Long> ids(Set<Alergeno> alergenos) {
		return alergenos.stream().map(Alergeno::getId).collect(Collectors.toSet());
	}

	private static final class ContagemRestricao {
		private final Long id;
		private final String nome;
		private int quantidade;

		private ContagemRestricao(Long id, String nome) {
			this.id = id;
			this.nome = nome;
		}
	}

}
