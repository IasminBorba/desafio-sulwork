package br.com.sulwork.eventos.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = { "11144477735", "111.444.777-35", " 111.444.777-35 ", "52998224725", "00000000191" })
	void aceitaCpfComDigitosVerificadoresCorretos(String cpf) {
		assertThat(CpfUtils.isValido(cpf)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "11144477734", "11144477725", "111.444.777-36", "52998224724" })
	void rejeitaDigitoVerificadorErrado(String cpf) {
		assertThat(CpfUtils.isValido(cpf)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "00000000000", "11111111111", "222.222.222-22", "99999999999" })
	void rejeitaTodosOsDigitosIguais(String cpf) {
		assertThat(CpfUtils.isValido(cpf)).isFalse();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "1114447773", "111444777350", "1114447773a", "111-444-777.35", "abc.def.ghi-jk" })
	void rejeitaFormatoInvalido(String cpf) {
		assertThat(CpfUtils.isValido(cpf)).isFalse();
	}

	@Test
	void normalizarRemoveMascara() {
		assertThat(CpfUtils.normalizar("111.444.777-35")).isEqualTo("11144477735");
		assertThat(CpfUtils.normalizar("11144477735")).isEqualTo("11144477735");
	}

	@Test
	void normalizarRetornaNullParaFormatoInvalido() {
		assertThat(CpfUtils.normalizar("111 444 777 35")).isNull();
		assertThat(CpfUtils.normalizar(null)).isNull();
	}

	@Test
	void mascararEscondeOMeioDoCpf() {
		assertThat(CpfUtils.mascarar("11144477735")).isEqualTo("111.***.***-35");
		assertThat(CpfUtils.mascarar("111.444.777-35")).isEqualTo("111.***.***-35");
	}

}
