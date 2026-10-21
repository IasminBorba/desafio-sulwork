package br.com.sulwork.eventos.meta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaCategoriaRepository extends JpaRepository<MetaCategoria, Long> {

	boolean existsByEvento_IdAndCategoria_Id(Long eventoId, Long categoriaId);

	@EntityGraph(attributePaths = "categoria")
	List<MetaCategoria> findByEvento_IdOrderByCategoria_NomeAsc(Long eventoId);

	@EntityGraph(attributePaths = "categoria")
	Optional<MetaCategoria> findByIdAndEvento_Id(Long id, Long eventoId);

}
