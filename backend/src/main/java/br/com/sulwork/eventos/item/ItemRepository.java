package br.com.sulwork.eventos.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ItemRepository extends JpaRepository<Item, Long> {

	boolean existsByEvento_Id(Long eventoId);

	boolean existsByEvento_IdAndCategoria_Id(Long eventoId, Long categoriaId);

	long countByEvento_IdAndCategoria_Id(Long eventoId, Long categoriaId);

	@EntityGraph(attributePaths = { "categoria", "alergenos" })
	List<Item> findByEvento_IdOrderByNomeAsc(Long eventoId);

	@EntityGraph(attributePaths = { "categoria", "alergenos" })
	Optional<Item> findByIdAndEvento_Id(Long id, Long eventoId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Item i join fetch i.evento where i.id = :id")
	Optional<Item> findByIdForUpdate(@Param("id") Long id);

}
