package br.com.sulwork.eventos.contribuicao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Long> {

	boolean existsByItem_Id(Long itemId);

	boolean existsByParticipacao_IdAndItem_Id(Long participacaoId, Long itemId);

	@Query("select coalesce(sum(c.quantidade), 0) from Contribuicao c where c.item.id = :itemId")
	int somaQuantidadePorItem(@Param("itemId") Long itemId);

	@Query("""
			select coalesce(sum(c.quantidade), 0)
			  from Contribuicao c
			 where c.item.id = :itemId
			   and c.id <> :excluindoId
			""")
	int somaQuantidadePorItemExcluindo(@Param("itemId") Long itemId, @Param("excluindoId") Long excluindoId);

	@EntityGraph(attributePaths = { "item", "participacao", "participacao.usuario" })
	List<Contribuicao> findByParticipacao_Evento_IdOrderByItem_NomeAsc(Long eventoId);

	@EntityGraph(attributePaths = { "item", "participacao", "participacao.usuario", "participacao.evento" })
	Optional<Contribuicao> findByIdAndParticipacao_Evento_Id(Long id, Long eventoId);

}
