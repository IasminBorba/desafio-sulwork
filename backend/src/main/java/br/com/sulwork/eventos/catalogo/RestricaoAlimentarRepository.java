package br.com.sulwork.eventos.catalogo;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestricaoAlimentarRepository extends JpaRepository<RestricaoAlimentar, Long> {

	@EntityGraph(attributePaths = "alergenos")
	Optional<RestricaoAlimentar> findByNome(String nome);

}
