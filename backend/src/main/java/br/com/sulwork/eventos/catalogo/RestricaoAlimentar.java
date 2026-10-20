package br.com.sulwork.eventos.catalogo;

import java.util.LinkedHashSet;
import java.util.Set;

import br.com.sulwork.eventos.shared.domain.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "restricao_alimentar", uniqueConstraints = @UniqueConstraint(name = "uk_restricao_alimentar_nome", columnNames = "nome"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class RestricaoAlimentar extends EntidadeBase {

	@Column(nullable = false, unique = true, length = 100)
	@ToString.Include
	private String nome;

	@ManyToMany
	@JoinTable(
			name = "restricao_alergeno",
			joinColumns = @JoinColumn(name = "restricao_id"),
			inverseJoinColumns = @JoinColumn(name = "alergeno_id"))
	@ToString.Exclude
	private Set<Alergeno> alergenos = new LinkedHashSet<>();

}
