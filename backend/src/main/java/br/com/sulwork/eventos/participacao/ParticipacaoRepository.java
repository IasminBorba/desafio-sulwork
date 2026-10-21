package br.com.sulwork.eventos.participacao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sulwork.eventos.painel.PainelContribuicaoProjecao;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

	boolean existsByEvento_IdAndUsuario_Id(Long eventoId, Long usuarioId);

	@EntityGraph(attributePaths = { "usuario", "usuario.restricoes" })
	Optional<Participacao> findByEvento_IdAndUsuario_Id(Long eventoId, Long usuarioId);

	@EntityGraph(attributePaths = { "usuario", "usuario.restricoes" })
	List<Participacao> findByEvento_IdOrderByUsuario_NomeAsc(Long eventoId);

	@EntityGraph(attributePaths = { "usuario", "evento" })
	Optional<Participacao> findByIdAndEvento_Id(Long id, Long eventoId);

	@Query(value = """
			select p.id as participacaoId,
			       u.id as usuarioId,
			       u.nome as usuarioNome,
			       u.cpf as cpf,
			       i.id as itemId,
			       i.nome as itemNome,
			       cat.nome as categoriaNome,
			       c.quantidade as quantidade
			  from participacao p
			  join usuario u on u.id = p.usuario_id
			  left join contribuicao c on c.participacao_id = p.id
			  left join item i on i.id = c.item_id
			  left join categoria cat on cat.id = i.categoria_id
			 where p.evento_id = :eventoId
			 order by u.nome, i.nome
			""", nativeQuery = true)
	List<PainelContribuicaoProjecao> listarPainelDoEvento(@Param("eventoId") Long eventoId);

}
