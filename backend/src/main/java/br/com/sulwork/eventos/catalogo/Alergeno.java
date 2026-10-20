package br.com.sulwork.eventos.catalogo;

import br.com.sulwork.eventos.shared.domain.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "alergeno", uniqueConstraints = @UniqueConstraint(name = "uk_alergeno_nome", columnNames = "nome"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Alergeno extends EntidadeBase {

	@Column(nullable = false, unique = true, length = 100)
	@ToString.Include
	private String nome;

}
