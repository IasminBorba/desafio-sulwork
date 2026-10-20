package br.com.sulwork.eventos.contribuicao;

import br.com.sulwork.eventos.item.Item;
import br.com.sulwork.eventos.participacao.Participacao;
import br.com.sulwork.eventos.shared.domain.EntidadeAuditavel;
import jakarta.persistence.Column;
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
@Table(name = "contribuicao", uniqueConstraints = @UniqueConstraint(name = "uk_contribuicao_participacao_item",
		columnNames = { "participacao_id", "item_id" }))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Contribuicao extends EntidadeAuditavel {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "participacao_id", nullable = false)
	@ToString.Exclude
	private Participacao participacao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	@ToString.Exclude
	private Item item;

	@Column(nullable = false)
	@ToString.Include
	private int quantidade;

	public static Contribuicao nova(Participacao participacao, Item item, int quantidade) {
		Contribuicao contribuicao = new Contribuicao();
		contribuicao.participacao = participacao;
		contribuicao.item = item;
		contribuicao.quantidade = quantidade;
		return contribuicao;
	}

}
