package br.com.sulwork.eventos.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmail(String email);

	@EntityGraph(attributePaths = "restricoes")
	@Query("select u from Usuario u where u.id = :id")
	Optional<Usuario> findComRestricoesById(Long id);

	boolean existsByCpf(String cpf);

	boolean existsByEmail(String email);

}
