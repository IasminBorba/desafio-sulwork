package br.com.sulwork.eventos.evento;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class StatusEventoTest {

	@ParameterizedTest
	@CsvSource({
			"RASCUNHO, ABERTO, true",
			"RASCUNHO, CANCELADO, true",
			"RASCUNHO, ENCERRADO, false",
			"RASCUNHO, REALIZADO, false",
			"ABERTO, ENCERRADO, true",
			"ABERTO, CANCELADO, true",
			"ABERTO, ABERTO, false",
			"ABERTO, RASCUNHO, false",
			"ENCERRADO, ABERTO, true",
			"ENCERRADO, REALIZADO, true",
			"ENCERRADO, CANCELADO, true",
			"ENCERRADO, RASCUNHO, false",
			"REALIZADO, CANCELADO, false",
			"CANCELADO, ABERTO, false"
	})
	void transicoesDaTabela(StatusEvento de, StatusEvento para, boolean permitido) {
		assertThat(de.podeIrPara(para)).isEqualTo(permitido);
	}

	@ParameterizedTest
	@EnumSource(value = StatusEvento.class, names = { "REALIZADO", "CANCELADO" })
	void estadosFinaisNaoTemSaida(StatusEvento status) {
		for (StatusEvento destino : StatusEvento.values()) {
			assertThat(status.podeIrPara(destino)).isFalse();
		}
	}

}
