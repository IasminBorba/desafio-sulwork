package br.com.sulwork.eventos.item;

import java.util.LinkedHashSet;
import java.util.Set;

import br.com.sulwork.eventos.catalogo.Alergeno;
import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.shared.domain.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Item extends EntidadeAuditavel {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "evento_id", nullable = false)
	@ToString.Exclude
	private Evento evento;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categoria_id", nullable = false)
	@ToString.Exclude
	private Categoria categoria;

	@Column(nullable = false, length = 150)
	@ToString.Include
	private String nome;

	@Column(name = "quantidade_necessaria", nullable = false)
	private int quantidadeNecessaria;

	@Column(nullable = false)
	private boolean obrigatorio;

	@ManyToMany
	@JoinTable(
			name = "item_alergeno",
			joinColumns = @JoinColumn(name = "item_id"),
			inverseJoinColumns = @JoinColumn(name = "alergeno_id"))
	@ToString.Exclude
	private Set<Alergeno> alergenos = new LinkedHashSet<>();

	public static Item novo(Evento evento, Categoria categoria, String nome, int quantidadeNecessaria,
			boolean obrigatorio) {
		Item item = new Item();
		item.evento = evento;
		item.categoria = categoria;
		item.nome = nome;
		item.quantidadeNecessaria = quantidadeNecessaria;
		item.obrigatorio = obrigatorio;
		return item;
	}

}
