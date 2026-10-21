package br.com.sulwork.eventos.contribuicao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Long> {

	boolean existsByItem_Id(Long itemId);

	@Query("select coalesce(sum(c.quantidade), 0) from Contribuicao c where c.item.id = :itemId")
	int somaQuantidadePorItem(@Param("itemId") Long itemId);

}
