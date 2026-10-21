package br.com.sulwork.eventos.meta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sulwork.eventos.cobertura.MetaCoberturaProjecao;

public interface MetaCategoriaRepository extends JpaRepository<MetaCategoria, Long> {

	boolean existsByEvento_IdAndCategoria_Id(Long eventoId, Long categoriaId);

	@EntityGraph(attributePaths = "categoria")
	List<MetaCategoria> findByEvento_IdOrderByCategoria_NomeAsc(Long eventoId);

	@EntityGraph(attributePaths = "categoria")
	Optional<MetaCategoria> findByIdAndEvento_Id(Long id, Long eventoId);

	@Query(value = """
			select mc.categoria_id as categoriaId,
			       cat.nome as categoriaNome,
			       mc.quantidade_meta as quantidadeMeta
			  from meta_categoria mc
			  join categoria cat on cat.id = mc.categoria_id
			 where mc.evento_id = :eventoId
			 order by cat.nome
			""", nativeQuery = true)
	List<MetaCoberturaProjecao> listarDoEvento(@Param("eventoId") Long eventoId);

}
