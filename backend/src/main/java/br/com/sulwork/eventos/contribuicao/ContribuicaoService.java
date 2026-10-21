package br.com.sulwork.eventos.contribuicao;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.contribuicao.dto.ContribuicaoRequest;
import br.com.sulwork.eventos.contribuicao.dto.ContribuicaoResponse;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.item.Item;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.participacao.Participacao;
import br.com.sulwork.eventos.participacao.ParticipacaoRepository;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.PermissaoNegadaException;
import br.com.sulwork.eventos.shared.exception.RecursoNaoEncontradoException;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;

@Service
public class ContribuicaoService {

	private final ContribuicaoRepository contribuicaoRepository;
	private final ParticipacaoRepository participacaoRepository;
	private final ItemRepository itemRepository;
	private final EventoAcesso eventoAcesso;
	private final UsuarioAutenticado usuarioAutenticado;
	private final Clock clock;

	public ContribuicaoService(ContribuicaoRepository contribuicaoRepository,
			ParticipacaoRepository participacaoRepository, ItemRepository itemRepository, EventoAcesso eventoAcesso,
			UsuarioAutenticado usuarioAutenticado, Clock clock) {
		this.contribuicaoRepository = contribuicaoRepository;
		this.participacaoRepository = participacaoRepository;
		this.itemRepository = itemRepository;
		this.eventoAcesso = eventoAcesso;
		this.usuarioAutenticado = usuarioAutenticado;
		this.clock = clock;
	}

	@Transactional
	public List<ContribuicaoResponse> listar(Long eventoId) {
		Evento evento = eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		return contribuicaoRepository.findByParticipacao_Evento_IdOrderByItem_NomeAsc(evento.getId()).stream()
				.map(ContribuicaoResponse::de)
				.toList();
	}

	@Transactional
	public ContribuicaoResponse criar(Long eventoId, ContribuicaoRequest request) {
		Evento evento = eventoAbertoParaAlteracoes(eventoId);
		Participacao participacao = participacaoDoUsuario(evento.getId());
		Item item = itemTravado(request.itemId(), evento.getId());
		if (contribuicaoRepository.existsByParticipacao_IdAndItem_Id(participacao.getId(), item.getId())) {
			throw new ConflitoException("Você já contribuiu com este item. Use a edição para alterar a quantidade.");
		}
		garantirCabe(item.getId(), request.quantidade(), item.getQuantidadeNecessaria(), null);
		Contribuicao contribuicao = Contribuicao.nova(participacao, item, request.quantidade());
		return ContribuicaoResponse.de(contribuicaoRepository.save(contribuicao));
	}

	@Transactional
	public ContribuicaoResponse atualizar(Long eventoId, Long contribuicaoId, ContribuicaoRequest request) {
		eventoAbertoParaAlteracoes(eventoId);
		Contribuicao contribuicao = carregarPropria(eventoId, contribuicaoId);
		Item item = itemTravado(request.itemId(), eventoId);
		if (!item.getId().equals(contribuicao.getItem().getId())) {
			throw new RegraNegocioException("A contribuição não pode ser transferida para outro item.");
		}
		garantirCabe(item.getId(), request.quantidade(), item.getQuantidadeNecessaria(), contribuicao.getId());
		contribuicao.setQuantidade(request.quantidade());
		return ContribuicaoResponse.de(contribuicao);
	}

	@Transactional
	public void excluir(Long eventoId, Long contribuicaoId) {
		eventoAbertoParaAlteracoes(eventoId);
		Contribuicao contribuicao = carregarPropria(eventoId, contribuicaoId);
		contribuicaoRepository.delete(contribuicao);
	}

	private Evento eventoAbertoParaAlteracoes(Long eventoId) {
		Evento evento = eventoAcesso.visivel(eventoId, usuarioAutenticado.id());
		if (!evento.aceitaAlteracoes(clock)) {
			throw new ConflitoException("O prazo para alterações neste evento já encerrou.");
		}
		return evento;
	}

	private Participacao participacaoDoUsuario(Long eventoId) {
		return participacaoRepository.findByEvento_IdAndUsuario_Id(eventoId, usuarioAutenticado.id())
				.orElseThrow(() -> new RegraNegocioException("Participe do evento primeiro."));
	}

	private Item itemTravado(Long itemId, Long eventoId) {
		Item item = itemRepository.findByIdForUpdate(itemId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Item", itemId));
		if (!item.getEvento().getId().equals(eventoId)) {
			throw new RegraNegocioException("O item não pertence a este evento.");
		}
		return item;
	}

	private Contribuicao carregarPropria(Long eventoId, Long contribuicaoId) {
		Contribuicao contribuicao = contribuicaoRepository.findByIdAndParticipacao_Evento_Id(contribuicaoId, eventoId)
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Contribuição", contribuicaoId));
		if (!contribuicao.getParticipacao().getUsuario().getId().equals(usuarioAutenticado.id())) {
			throw new PermissaoNegadaException("Só é possível alterar a própria contribuição.");
		}
		return contribuicao;
	}

	private void garantirCabe(Long itemId, int quantidade, int necessaria, Long excluindoId) {
		int jaContribuido = excluindoId == null
				? contribuicaoRepository.somaQuantidadePorItem(itemId)
				: contribuicaoRepository.somaQuantidadePorItemExcluindo(itemId, excluindoId);
		int restam = necessaria - jaContribuido;
		if (quantidade > restam) {
			throw new ConflitoException("A quantidade ultrapassa o que ainda falta. Restam %d.".formatted(restam));
		}
	}

}
