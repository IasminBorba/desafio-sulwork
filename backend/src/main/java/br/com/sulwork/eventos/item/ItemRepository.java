package br.com.sulwork.eventos.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

	boolean existsByEvento_Id(Long eventoId);

	boolean existsByEvento_IdAndCategoria_Id(Long eventoId, Long categoriaId);

	long countByEvento_IdAndCategoria_Id(Long eventoId, Long categoriaId);

	@EntityGraph(attributePaths = { "categoria", "alergenos" })
	List<Item> findByEvento_IdOrderByNomeAsc(Long eventoId);

	@EntityGraph(attributePaths = { "categoria", "alergenos" })
	Optional<Item> findByIdAndEvento_Id(Long id, Long eventoId);

}
