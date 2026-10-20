package br.com.sulwork.eventos.evento;

import java.time.Instant;

import br.com.sulwork.eventos.shared.domain.EntidadeAuditavel;
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
		evento.prazoAlteracao = prazoAlteracao;
		evento.status = StatusEvento.RASCUNHO;
		evento.organizador = organizador;
		return evento;
	}

}
