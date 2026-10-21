package br.com.sulwork.eventos.evento;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoRepository extends JpaRepository<Evento, Long> {

	@EntityGraph(attributePaths = "organizador")
	@Query("select e from Evento e where e.id = :id")
	Optional<Evento> findComOrganizadorById(Long id);

	@EntityGraph(attributePaths = "organizador")
	Page<Evento> findByOrganizador_Id(Long organizadorId, Pageable pageable);

	@EntityGraph(attributePaths = "organizador")
	Page<Evento> findByOrganizador_IdAndStatus(Long organizadorId, StatusEvento status, Pageable pageable);

	@EntityGraph(attributePaths = "organizador")
	@Query("""
			select e from Evento e
			 where exists (select 1 from Participacao p where p.evento = e and p.usuario.id = :usuarioId)
			   and (:status is null or e.status = :status)
			""")
	Page<Evento> findParticipando(@Param("usuarioId") Long usuarioId, @Param("status") StatusEvento status,
			Pageable pageable);

	@EntityGraph(attributePaths = "organizador")
	@Query("""
			select e from Evento e
			 where (e.organizador.id = :usuarioId
			     or exists (select 1 from Participacao p where p.evento = e and p.usuario.id = :usuarioId))
			   and (e.status <> br.com.sulwork.eventos.evento.StatusEvento.RASCUNHO or e.organizador.id = :usuarioId)
			   and (:status is null or e.status = :status)
			""")
	Page<Evento> findVisiveisDoUsuario(@Param("usuarioId") Long usuarioId, @Param("status") StatusEvento status,
			Pageable pageable);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
			UPDATE evento
			   SET status = 'ENCERRADO',
			       atualizado_em = :agora
			 WHERE status = 'ABERTO'
			   AND prazo_alteracao < :agora
			""", nativeQuery = true)
	int encerrarVencidos(@Param("agora") Instant agora);

}
