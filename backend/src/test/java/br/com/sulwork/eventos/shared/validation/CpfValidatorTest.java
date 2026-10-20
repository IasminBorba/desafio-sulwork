package br.com.sulwork.eventos.shared.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CpfValidatorTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	record Cadastro(@CpfValido String cpf) {
	}

	@BeforeAll
	static void setUp() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void tearDown() {
		factory.close();
	}

	@Test
	void cpfValidoNaoGeraViolacao() {
		assertThat(validator.validate(new Cadastro("111.444.777-35"))).isEmpty();
	}

	@Test
	void cpfInvalidoGeraViolacaoComMensagem() {
		var violacoes = validator.validate(new Cadastro("111.111.111-11"));

		assertThat(violacoes).singleElement()
				.satisfies(v -> {
					assertThat(v.getPropertyPath()).hasToString("cpf");
					assertThat(v.getMessage()).isEqualTo("CPF inválido");
				});
	}

	@Test
	void nuloFicaParaONotBlank() {
		assertThat(validator.validate(new Cadastro(null))).isEmpty();
	}

}
