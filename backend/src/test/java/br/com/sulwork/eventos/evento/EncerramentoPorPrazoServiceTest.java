package br.com.sulwork.eventos.evento;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EncerramentoPorPrazoServiceTest {

	@Mock
	private EventoRepository eventoRepository;

	@Mock
	private Clock clock;

	@InjectMocks
	private EncerramentoPorPrazoService service;

	@Test
	void encaminhaOInstanteDoRelogioParaOUpdate() {
		Instant agora = Instant.parse("2026-06-15T13:01:00Z");
		when(clock.instant()).thenReturn(agora);

		service.encerrarVencidos();

		verify(eventoRepository).encerrarVencidos(agora);
	}

	@Test
	void relogioFixoUsaOMesmoInstante() {
		Clock fixo = Clock.fixed(Instant.parse("2026-06-15T13:00:00Z"), ZoneOffset.UTC);
		EncerramentoPorPrazoService comRelogioFixo = new EncerramentoPorPrazoService(eventoRepository, fixo);

		comRelogioFixo.encerrarVencidos();

		verify(eventoRepository).encerrarVencidos(Instant.parse("2026-06-15T13:00:00Z"));
	}

}
