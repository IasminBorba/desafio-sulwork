package br.com.sulwork.eventos.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public final class RelogioAjustavel extends Clock {

	public static final Instant INICIO = Instant.parse("2026-06-15T12:00:00Z");
	public static final ZoneId FUSO = ZoneId.of("America/Recife");

	private Instant instant;
	private final ZoneId zone;

	public RelogioAjustavel() {
		this(INICIO, FUSO);
	}

	public RelogioAjustavel(Instant instant, ZoneId zone) {
		this.instant = instant;
		this.zone = zone;
	}

	public void reset() {
		this.instant = INICIO;
	}

	public void definir(Instant instant) {
		this.instant = instant;
	}

	public void avancar(Duration duracao) {
		this.instant = this.instant.plus(duracao);
	}

	@Override
	public ZoneId getZone() {
		return zone;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new RelogioAjustavel(instant, zone);
	}

	@Override
	public Instant instant() {
		return instant;
	}

}
