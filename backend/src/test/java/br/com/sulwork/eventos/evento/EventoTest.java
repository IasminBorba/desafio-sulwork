package br.com.sulwork.eventos.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.RegraNegocioException;
import br.com.sulwork.eventos.usuario.Usuario;

class EventoTest {

	private static final Instant AGORA = Instant.parse("2026-06-15T15:00:00Z");
	private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

	@Test
	void novoEventoNasceComoRascunho() {
		assertThat(eventoRascunho().getStatus()).isEqualTo(StatusEvento.RASCUNHO);
	}

	@Test
	void publicarComItemEPrazoFuturoAbreOEvento() {
		Evento evento = eventoRascunho();

		evento.publicar(RELOGIO, true);

		assertThat(evento.getStatus()).isEqualTo(StatusEvento.ABERTO);
	}

	@Test
	void publicarSemItemRetorna422() {
		Evento evento = eventoRascunho();

		assertThatThrownBy(() -> evento.publicar(RELOGIO, false))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("Publique o evento somente depois de cadastrar pelo menos um item.");
		assertThat(evento.getStatus()).isEqualTo(StatusEvento.RASCUNHO);
	}

	@Test
	void publicarComPrazoPassadoRetorna422() {
		Evento evento = Evento.novo(
				"Churrasco",
				null,
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(2)),
				AGORA.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
				AGORA.minus(Duration.ofHours(1)),
				organizador());

		assertThatThrownBy(() -> evento.publicar(RELOGIO, true))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("O prazo para alterações precisa estar no futuro.");
	}

	@ParameterizedTest
	@EnumSource(value = StatusEvento.class, names = { "ABERTO", "ENCERRADO", "REALIZADO", "CANCELADO" })
	void publicarForaDeRascunhoRetorna409(StatusEvento statusAtual) {
		Evento evento = eventoNoStatus(statusAtual);

		assertThatThrownBy(() -> evento.publicar(RELOGIO, true))
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void encerrarEventoAberto() {
		Evento evento = eventoAberto();

		evento.encerrar();

		assertThat(evento.getStatus()).isEqualTo(StatusEvento.ENCERRADO);
	}

	@ParameterizedTest
	@EnumSource(value = StatusEvento.class, names = { "RASCUNHO", "ENCERRADO", "REALIZADO", "CANCELADO" })
	void encerrarForaDeAbertoRetorna409(StatusEvento statusAtual) {
		Evento evento = eventoNoStatus(statusAtual);

		assertThatThrownBy(evento::encerrar)
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void cancelarRascunhoAbertoOuEncerrado() {
		Evento rascunho = eventoRascunho();
		rascunho.cancelar();
		assertThat(rascunho.getStatus()).isEqualTo(StatusEvento.CANCELADO);

		Evento aberto = eventoAberto();
		aberto.cancelar();
		assertThat(aberto.getStatus()).isEqualTo(StatusEvento.CANCELADO);

		Evento encerrado = eventoEncerrado();
		encerrado.cancelar();
		assertThat(encerrado.getStatus()).isEqualTo(StatusEvento.CANCELADO);
	}

	@ParameterizedTest
	@EnumSource(value = StatusEvento.class, names = { "REALIZADO", "CANCELADO" })
	void cancelarEstadoFinalRetorna409(StatusEvento statusAtual) {
		Evento evento = eventoNoStatus(statusAtual);

		assertThatThrownBy(evento::cancelar)
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void concluirDepoisDaDataDeInicio() {
		Evento evento = eventoEncerrado();
		Clock depoisDoInicio = Clock.offset(RELOGIO, Duration.ofDays(2).plusHours(1));

		evento.concluir(depoisDoInicio);

		assertThat(evento.getStatus()).isEqualTo(StatusEvento.REALIZADO);
	}

	@Test
	void concluirNoInstanteDaDataDeInicio() {
		Evento evento = eventoEncerrado();
		Clock noInicio = Clock.offset(RELOGIO, Duration.ofDays(2));

		evento.concluir(noInicio);

		assertThat(evento.getStatus()).isEqualTo(StatusEvento.REALIZADO);
	}

	@Test
	void concluirAntesDaDataDeInicioRetorna422() {
		Evento evento = eventoEncerrado();

		assertThatThrownBy(() -> evento.concluir(RELOGIO))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("Só é possível concluir o evento depois da data de início.");
		assertThat(evento.getStatus()).isEqualTo(StatusEvento.ENCERRADO);
	}

	@ParameterizedTest
	@EnumSource(value = StatusEvento.class, names = { "RASCUNHO", "ABERTO", "REALIZADO", "CANCELADO" })
	void concluirForaDeEncerradoRetorna409(StatusEvento statusAtual) {
		Evento evento = eventoNoStatus(statusAtual);
		Clock depoisDoInicio = Clock.offset(RELOGIO, Duration.ofDays(2));

		assertThatThrownBy(() -> evento.concluir(depoisDoInicio))
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void reabrirSemPrazoRetorna422() {
		Evento evento = eventoEncerrado();

		assertThatThrownBy(() -> evento.reabrir(RELOGIO, null, null, null))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("Informe o novo prazo para alterações.");
	}

	@Test
	void reabrirComPrazoNoPassadoRetorna422() {
		Evento evento = eventoEncerrado();

		assertThatThrownBy(() -> evento.reabrir(RELOGIO, AGORA.minusSeconds(1), null, null))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("O novo prazo para alterações não pode ser anterior ao momento atual.");
	}

	@Test
	void reabrirComDataInicioPassadaSemNovasDatasRetorna422() {
		Evento evento = eventoEncerrado();
		Clock depoisDoInicio = Clock.offset(RELOGIO, Duration.ofDays(3));

		assertThatThrownBy(() -> evento.reabrir(depoisDoInicio, depoisDoInicio.instant().plus(Duration.ofDays(1)), null, null))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("o evento já passou da data de início; informe novas datas");
	}

	@Test
	void reabrirComDataInicioPassadaENovasDatasValidasAbreOEvento() {
		Evento evento = eventoEncerrado();
		Clock depoisDoInicio = Clock.offset(RELOGIO, Duration.ofDays(3));
		Instant novoPrazo = depoisDoInicio.instant().plus(Duration.ofDays(1));
		Instant novaInicio = depoisDoInicio.instant().plus(Duration.ofDays(2));
		Instant novaFim = novaInicio.plus(Duration.ofHours(4));

		evento.reabrir(depoisDoInicio, novoPrazo, novaInicio, novaFim);

		assertThat(evento.getStatus()).isEqualTo(StatusEvento.ABERTO);
		assertThat(evento.getPrazoAlteracao()).isEqualTo(novoPrazo);
		assertThat(evento.getDataInicio()).isEqualTo(novaInicio);
		assertThat(evento.getDataFim()).isEqualTo(novaFim);
	}

	@Test
	void reabrirComDataInicioNoFuturoESoOPrazoNovoAbreOEvento() {
		Evento evento = eventoEncerrado();
		Instant novoPrazo = AGORA.plus(Duration.ofHours(12));

		evento.reabrir(RELOGIO, novoPrazo, null, null);

		assertThat(evento.getStatus()).isEqualTo(StatusEvento.ABERTO);
		assertThat(evento.getPrazoAlteracao()).isEqualTo(novoPrazo);
		assertThat(evento.getDataInicio()).isEqualTo(AGORA.plus(Duration.ofDays(2)));
	}

	@Test
	void reabrirComPrazoDepoisDaDataDeInicioRetorna422() {
		Evento evento = eventoEncerrado();
		Instant prazoDepoisDoInicio = AGORA.plus(Duration.ofDays(3));

		assertThatThrownBy(() -> evento.reabrir(RELOGIO, prazoDepoisDoInicio, null, null))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("O prazo para alterações precisa ser igual ou anterior à data de início.");
	}

	@ParameterizedTest
	@EnumSource(value = StatusEvento.class, names = { "RASCUNHO", "ABERTO", "REALIZADO", "CANCELADO" })
	void reabrirForaDeEncerradoRetorna409(StatusEvento statusAtual) {
		Evento evento = eventoNoStatus(statusAtual);

		assertThatThrownBy(() -> evento.reabrir(RELOGIO, AGORA.plus(Duration.ofHours(12)), null, null))
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void aceitaAlteracoesSoQuandoAbertoEDentroDoPrazo() {
		Evento aberto = eventoAberto();
		assertThat(aberto.aceitaAlteracoes(RELOGIO)).isTrue();
		assertThat(aberto.aceitaAlteracoes(Clock.offset(RELOGIO, Duration.ofDays(1)))).isTrue();
		assertThat(aberto.aceitaAlteracoes(Clock.offset(RELOGIO, Duration.ofDays(1).plusSeconds(1)))).isFalse();

		Evento encerrado = eventoEncerrado();
		assertThat(encerrado.aceitaAlteracoes(RELOGIO)).isFalse();
	}

	private static Evento eventoRascunho() {
		return Evento.novo(
				"Churrasco da equipe",
				"Celebração",
				TipoEvento.CHURRASCO,
				AGORA.plus(Duration.ofDays(2)),
				AGORA.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
				AGORA.plus(Duration.ofDays(1)),
				organizador());
	}

	private static Evento eventoAberto() {
		Evento evento = eventoRascunho();
		evento.publicar(RELOGIO, true);
		return evento;
	}

	private static Evento eventoEncerrado() {
		Evento evento = eventoAberto();
		evento.encerrar();
		return evento;
	}

	private static Evento eventoNoStatus(StatusEvento status) {
		return switch (status) {
			case RASCUNHO -> eventoRascunho();
			case ABERTO -> eventoAberto();
			case ENCERRADO -> eventoEncerrado();
			case REALIZADO -> {
				Evento evento = eventoEncerrado();
				evento.concluir(Clock.offset(RELOGIO, Duration.ofDays(2)));
				yield evento;
			}
			case CANCELADO -> {
				Evento evento = eventoRascunho();
				evento.cancelar();
				yield evento;
			}
		};
	}

	private static Usuario organizador() {
		return Usuario.novo("Ana", "11144477735", "ana@example.com", "hash");
	}

}
