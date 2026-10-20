package br.com.sulwork.eventos.meta;

import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.evento.Evento;
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
@Table(name = "meta_categoria", uniqueConstraints = @UniqueConstraint(name = "uk_meta_categoria_evento_categoria",
		columnNames = { "evento_id", "categoria_id" }))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class MetaCategoria extends EntidadeAuditavel {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "evento_id", nullable = false)
	@ToString.Exclude
	private Evento evento;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categoria_id", nullable = false)
	@ToString.Exclude
	private Categoria categoria;

	@Column(name = "quantidade_meta", nullable = false)
	@ToString.Include
	private int quantidadeMeta;

	public static MetaCategoria nova(Evento evento, Categoria categoria, int quantidadeMeta) {
		MetaCategoria meta = new MetaCategoria();
		meta.evento = evento;
		meta.categoria = categoria;
		meta.quantidadeMeta = quantidadeMeta;
		return meta;
	}

}
