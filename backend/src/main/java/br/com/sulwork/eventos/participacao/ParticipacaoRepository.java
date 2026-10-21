package br.com.sulwork.eventos.participacao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

	boolean existsByEvento_IdAndUsuario_Id(Long eventoId, Long usuarioId);

}
