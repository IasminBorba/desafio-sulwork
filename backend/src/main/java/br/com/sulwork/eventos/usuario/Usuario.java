package br.com.sulwork.eventos.usuario;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import br.com.sulwork.eventos.catalogo.RestricaoAlimentar;
import br.com.sulwork.eventos.shared.domain.EntidadeAuditavel;
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
@Table(name = "usuario", uniqueConstraints = {
		@UniqueConstraint(name = "uk_usuario_cpf", columnNames = "cpf"),
		@UniqueConstraint(name = "uk_usuario_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Usuario extends EntidadeAuditavel {

	@Column(nullable = false, length = 150)
	@ToString.Include
	private String nome;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, unique = true, length = 11)
	private String cpf;

	@Column(nullable = false, unique = true, length = 255)
	@ToString.Include
	private String email;

	@Column(name = "senha_hash", nullable = false, length = 100)
	private String senhaHash;

	@ManyToMany
	@JoinTable(
			name = "usuario_restricao",
			joinColumns = @JoinColumn(name = "usuario_id"),
			inverseJoinColumns = @JoinColumn(name = "restricao_id"))
	@ToString.Exclude
	private Set<RestricaoAlimentar> restricoes = new LinkedHashSet<>();

	public static Usuario novo(String nome, String cpf, String email, String senhaHash) {
		Usuario usuario = new Usuario();
		usuario.nome = nome;
		usuario.cpf = cpf;
		usuario.email = email;
		usuario.senhaHash = senhaHash;
		return usuario;
	}

	public void atualizarPerfil(String nome, Set<RestricaoAlimentar> novasRestricoes) {
		this.nome = nome;
		this.restricoes.clear();
		this.restricoes.addAll(novasRestricoes);
	}

}
