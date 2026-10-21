package br.com.sulwork.eventos.evento;

import java.time.Clock;
import java.time.Instant;

import br.com.sulwork.eventos.shared.domain.EntidadeAuditavel;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "evento")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Evento extends EntidadeAuditavel {

	@Column(nullable = false, length = 150)
	@ToString.Include
	private String nome;

	@Column(columnDefinition = "text")
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TipoEvento tipo;

	@Column(name = "data_inicio", nullable = false)
	private Instant dataInicio;

	@Column(name = "data_fim", nullable = false)
	private Instant dataFim;

	@Column(name = "prazo_alteracao", nullable = false)
	private Instant prazoAlteracao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Setter(AccessLevel.NONE)
	@ToString.Include
	private StatusEvento status = StatusEvento.RASCUNHO;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organizador_id", nullable = false)
	@ToString.Exclude
	private Usuario organizador;

	public static Evento novo(String nome, String descricao, TipoEvento tipo, Instant dataInicio, Instant dataFim,
			Instant prazoAlteracao, Usuario organizador) {
		Evento evento = new Evento();
		evento.nome = nome;
		evento.descricao = descricao;
		evento.tipo = tipo;
		evento.dataInicio = dataInicio;
		evento.dataFim = dataFim;
		validarDatas(prazoAlteracao, dataInicio, dataFim);
		evento.prazoAlteracao = prazoAlteracao;
		evento.status = StatusEvento.RASCUNHO;
		evento.organizador = organizador;
		return evento;
	}

	public void atualizar(String nome, String descricao, TipoEvento tipo, Instant dataInicio, Instant dataFim,
			Instant prazoAlteracao) {
		if (status != StatusEvento.RASCUNHO && status != StatusEvento.ABERTO) {
			throw new ConflitoException("Os dados do evento só podem ser editados em rascunho ou aberto.");
		}
		validarDatas(prazoAlteracao, dataInicio, dataFim);
		this.nome = nome;
		this.descricao = descricao;
		this.tipo = tipo;
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;
		this.prazoAlteracao = prazoAlteracao;
	}

	public void garantirExclusao() {
		if (status != StatusEvento.RASCUNHO) {
			throw new ConflitoException("Só é possível excluir um evento em rascunho.");
		}
	}

	public boolean ehOrganizadoPor(Long usuarioId) {
		return organizador.getId().equals(usuarioId);
	}

	public void publicar(Clock clock, boolean possuiPeloMenosUmItem) {
		if (status != StatusEvento.RASCUNHO) {
			throw new ConflitoException("Só é possível publicar um evento em rascunho.");
		}
		if (!possuiPeloMenosUmItem) {
			throw new RegraNegocioException("Publique o evento somente depois de cadastrar pelo menos um item.");
		}
		if (!prazoAlteracao.isAfter(clock.instant())) {
			throw new RegraNegocioException("O prazo para alterações precisa estar no futuro.");
		}
		transitarPara(StatusEvento.ABERTO);
	}

	public void encerrar() {
		transitarPara(StatusEvento.ENCERRADO);
	}

	public void reabrir(Clock clock, Instant novoPrazo, Instant novaDataInicio, Instant novaDataFim) {
		if (status != StatusEvento.ENCERRADO) {
			throw new ConflitoException("Só é possível reabrir um evento encerrado.");
		}
		Instant agora = clock.instant();
		if (novoPrazo == null) {
			throw new RegraNegocioException("Informe o novo prazo para alterações.");
		}
		if (novoPrazo.isBefore(agora)) {
			throw new RegraNegocioException("O novo prazo para alterações não pode ser anterior ao momento atual.");
		}

		boolean dataInicioJaPassou = dataInicio.isBefore(agora);
		Instant dataInicioFinal = dataInicio;
		Instant dataFimFinal = dataFim;

		if (dataInicioJaPassou) {
			if (novaDataInicio == null || novaDataFim == null) {
				throw new RegraNegocioException("o evento já passou da data de início; informe novas datas");
			}
			dataInicioFinal = novaDataInicio;
			dataFimFinal = novaDataFim;
		} else if (novaDataInicio != null || novaDataFim != null) {
			if (novaDataInicio == null || novaDataFim == null) {
				throw new RegraNegocioException("Informe a nova data de início e a nova data de fim.");
			}
			dataInicioFinal = novaDataInicio;
			dataFimFinal = novaDataFim;
		}

		if (dataInicioFinal.isBefore(agora)) {
			throw new RegraNegocioException("A data de início não pode ser anterior ao momento atual.");
		}
		validarDatas(novoPrazo, dataInicioFinal, dataFimFinal);

		this.prazoAlteracao = novoPrazo;
		this.dataInicio = dataInicioFinal;
		this.dataFim = dataFimFinal;
		transitarPara(StatusEvento.ABERTO);
	}

	public void concluir(Clock clock) {
		if (!status.podeIrPara(StatusEvento.REALIZADO)) {
			throw new ConflitoException("Só é possível concluir um evento encerrado.");
		}
		if (clock.instant().isBefore(dataInicio)) {
			throw new RegraNegocioException("Só é possível concluir o evento depois da data de início.");
		}
		transitarPara(StatusEvento.REALIZADO);
	}

	public void cancelar() {
		transitarPara(StatusEvento.CANCELADO);
	}

	public boolean aceitaAlteracoes(Clock clock) {
		return status == StatusEvento.ABERTO && !clock.instant().isAfter(prazoAlteracao);
	}

	private void transitarPara(StatusEvento destino) {
		if (!status.podeIrPara(destino)) {
			throw new ConflitoException(
					"Não é possível alterar o status de %s para %s.".formatted(status, destino));
		}
		this.status = destino;
	}

	private static void validarDatas(Instant prazo, Instant inicio, Instant fim) {
		if (prazo.isAfter(inicio)) {
			throw new RegraNegocioException("O prazo para alterações precisa ser igual ou anterior à data de início.");
		}
		if (fim.isBefore(inicio)) {
			throw new RegraNegocioException("A data de fim precisa ser igual ou posterior à data de início.");
		}
	}

}
