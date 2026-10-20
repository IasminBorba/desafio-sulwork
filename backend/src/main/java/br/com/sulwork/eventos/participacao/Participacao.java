package br.com.sulwork.eventos.participacao;

import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.shared.domain.EntidadeBase;
import br.com.sulwork.eventos.usuario.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "participacao", uniqueConstraints = @UniqueConstraint(name = "uk_participacao_usuario_evento",
		columnNames = { "usuario_id", "evento_id" }))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Participacao extends EntidadeBase {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	@ToString.Exclude
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "evento_id", nullable = false)
	@ToString.Exclude
	private Evento evento;

	public static Participacao de(Usuario usuario, Evento evento) {
		Participacao participacao = new Participacao();
		participacao.usuario = usuario;
		participacao.evento = evento;
		return participacao;
	}

}
