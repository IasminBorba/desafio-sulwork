package br.com.sulwork.eventos.evento;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alinha o status gravado com o prazo. Fica em um bean separado para o
 * {@code @Transactional} do {@code UPDATE} não cair na auto-invocação do Spring.
 */
@Service
public class EncerramentoPorPrazoService {

	private final EventoRepository eventoRepository;
	private final Clock clock;

	public EncerramentoPorPrazoService(EventoRepository eventoRepository, Clock clock) {
		this.eventoRepository = eventoRepository;
		this.clock = clock;
	}

	@Transactional
	public int encerrarVencidos() {
		return eventoRepository.encerrarVencidos(clock.instant());
	}

}
