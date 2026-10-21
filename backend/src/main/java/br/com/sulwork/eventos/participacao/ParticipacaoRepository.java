package br.com.sulwork.eventos.participacao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

	boolean existsByEvento_IdAndUsuario_Id(Long eventoId, Long usuarioId);

	@EntityGraph(attributePaths = { "usuario", "usuario.restricoes" })
	Optional<Participacao> findByEvento_IdAndUsuario_Id(Long eventoId, Long usuarioId);

	@EntityGraph(attributePaths = { "usuario", "usuario.restricoes" })
	List<Participacao> findByEvento_IdOrderByUsuario_NomeAsc(Long eventoId);

	@EntityGraph(attributePaths = { "usuario", "evento" })
	Optional<Participacao> findByIdAndEvento_Id(Long id, Long eventoId);

}
