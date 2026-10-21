package br.com.sulwork.eventos.painel;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.cobertura.CoberturaService;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoAcesso;
import br.com.sulwork.eventos.evento.dto.EventoDetalheResponse;
import br.com.sulwork.eventos.painel.dto.PainelContribuicaoItemResponse;
import br.com.sulwork.eventos.painel.dto.PainelParticipanteResponse;
import br.com.sulwork.eventos.painel.dto.PainelResponse;
import br.com.sulwork.eventos.participacao.Participacao;
import br.com.sulwork.eventos.participacao.ParticipacaoRepository;
import br.com.sulwork.eventos.restricao.RestricaoEventoService;
import br.com.sulwork.eventos.shared.domain.CpfUtils;
import br.com.sulwork.eventos.usuario.UsuarioAutenticado;
import br.com.sulwork.eventos.usuario.dto.RestricaoResumoResponse;

@Service
public class PainelService {

	private final EventoAcesso eventoAcesso;
	private final ParticipacaoRepository participacaoRepository;
	private final CoberturaService coberturaService;
	private final RestricaoEventoService restricaoEventoService;
	private final UsuarioAutenticado usuarioAutenticado;
	private final Clock clock;

	public PainelService(EventoAcesso eventoAcesso, ParticipacaoRepository participacaoRepository,
			CoberturaService coberturaService, RestricaoEventoService restricaoEventoService,
			UsuarioAutenticado usuarioAutenticado, Clock clock) {
		this.eventoAcesso = eventoAcesso;
		this.participacaoRepository = participacaoRepository;
		this.coberturaService = coberturaService;
		this.restricaoEventoService = restricaoEventoService;
		this.usuarioAutenticado = usuarioAutenticado;
		this.clock = clock;
	}

	@Transactional
	public PainelResponse montar(Long eventoId) {
		Long usuarioId = usuarioAutenticado.id();
		Evento evento = eventoAcesso.comoOrganizador(eventoId, usuarioId);
		boolean participando = participacaoRepository.existsByEvento_IdAndUsuario_Id(eventoId, usuarioId);

		Map<Long, PainelParticipanteBuilder> participantes = new LinkedHashMap<>();
		for (Participacao participacao : participacaoRepository.findByEvento_IdOrderByUsuario_NomeAsc(eventoId)) {
			participantes.put(participacao.getId(), new PainelParticipanteBuilder(participacao));
		}
		for (PainelContribuicaoProjecao linha : participacaoRepository.listarPainelDoEvento(eventoId)) {
			PainelParticipanteBuilder builder = participantes.computeIfAbsent(linha.getParticipacaoId(),
					id -> new PainelParticipanteBuilder(linha));
			if (linha.getItemId() != null) {
				builder.contribuicoes.add(new PainelContribuicaoItemResponse(
						linha.getItemId(),
						linha.getItemNome(),
						linha.getCategoriaNome(),
						linha.getQuantidade()));
			}
		}

		return new PainelResponse(
				EventoDetalheResponse.de(evento, usuarioId, participando, clock),
				coberturaService.calcularSemAutorizacao(eventoId),
				restricaoEventoService.analisarSemAutorizacao(eventoId),
				participantes.values().stream().map(PainelParticipanteBuilder::montar).toList());
	}

	private static final class PainelParticipanteBuilder {
		private final Long participacaoId;
		private final Long usuarioId;
		private final String nome;
		private final String cpf;
		private final List<RestricaoResumoResponse> restricoes;
		private final List<PainelContribuicaoItemResponse> contribuicoes = new ArrayList<>();

		private PainelParticipanteBuilder(Participacao participacao) {
			this.participacaoId = participacao.getId();
			this.usuarioId = participacao.getUsuario().getId();
			this.nome = participacao.getUsuario().getNome();
			this.cpf = CpfUtils.mascarar(participacao.getUsuario().getCpf());
			this.restricoes = participacao.getUsuario().getRestricoes().stream()
					.map(restricao -> new RestricaoResumoResponse(restricao.getId(), restricao.getNome()))
					.toList();
		}

		private PainelParticipanteBuilder(PainelContribuicaoProjecao linha) {
			this.participacaoId = linha.getParticipacaoId();
			this.usuarioId = linha.getUsuarioId();
			this.nome = linha.getUsuarioNome();
			this.cpf = CpfUtils.mascarar(linha.getCpf());
			this.restricoes = List.of();
		}

		private PainelParticipanteResponse montar() {
			return new PainelParticipanteResponse(
					participacaoId,
					usuarioId,
					nome,
					cpf,
					contribuicoes.isEmpty(),
					restricoes,
					List.copyOf(contribuicoes));
		}
	}

}
