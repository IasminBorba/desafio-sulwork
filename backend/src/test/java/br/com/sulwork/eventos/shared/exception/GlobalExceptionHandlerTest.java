package br.com.sulwork.eventos.shared.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void recursoNaoEncontradoRetorna404() throws Exception {
		mockMvc.perform(get("/teste/nao-encontrado"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.detail").value("Evento com id 42 não encontrado(a)."));
	}

	@Test
	void permissaoNegadaRetorna403() throws Exception {
		mockMvc.perform(get("/teste/permissao"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void conflitoRetorna409() throws Exception {
		mockMvc.perform(get("/teste/conflito"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Evento já está encerrado."));
	}

	@Test
	void regraNegocioRetorna422() throws Exception {
		mockMvc.perform(get("/teste/regra"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.status").value(422));
	}

	@Test
	void erroInesperadoRetorna500SemExporDetalhes() throws Exception {
		mockMvc.perform(get("/teste/inesperado"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado."));
	}

	@Test
	void ordenacaoInvalidaDoSpringDataRetorna400() throws Exception {
		mockMvc.perform(get("/teste/ordenacao"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Dados inválidos"))
				.andExpect(jsonPath("$.detail").value(containsString("ordenação")));
	}

	@Test
	void acessoInvalidoNaoRelacionadoAOrdenacaoRetorna500() throws Exception {
		mockMvc.perform(get("/teste/acesso-invalido"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado."));
	}

	@Test
	void validacaoRetorna400ComErrosPorCampo() throws Exception {
		mockMvc.perform(post("/teste/validacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Dados inválidos"))
				.andExpect(jsonPath("$.erros[0].campo").value("nome"))
				.andExpect(jsonPath("$.erros[0].mensagem").value("é obrigatório"));
	}

	record RequisicaoDeTeste(@NotBlank(message = "é obrigatório") String nome) {
	}

	@RestController
	static class ControllerDeTeste {

		@GetMapping("/teste/nao-encontrado")
		void naoEncontrado() {
			throw RecursoNaoEncontradoException.de("Evento", 42);
		}

		@GetMapping("/teste/permissao")
		void permissao() {
			throw new PermissaoNegadaException("Apenas o organizador pode alterar o evento.");
		}

		@GetMapping("/teste/conflito")
		void conflito() {
			throw new ConflitoException("Evento já está encerrado.");
		}

		@GetMapping("/teste/regra")
		void regra() {
			throw new RegraNegocioException("Item não pertence ao evento.");
		}

		@GetMapping("/teste/inesperado")
		void inesperado() {
			throw new IllegalStateException("detalhe interno");
		}

		@GetMapping("/teste/ordenacao")
		void ordenacao() {
			throw new InvalidDataAccessApiUsageException(
					"Sort expression '[\"string\"]: ASC' must only contain property references or aliases used in the select clause");
		}

		@GetMapping("/teste/acesso-invalido")
		void acessoInvalido() {
			throw new InvalidDataAccessApiUsageException("Query specified join fetching");
		}

		@PostMapping("/teste/validacao")
		void validacao(@Valid @RequestBody RequisicaoDeTeste requisicao) {
		}

	}

}
