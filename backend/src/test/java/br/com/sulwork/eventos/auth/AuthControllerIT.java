package br.com.sulwork.eventos.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.support.IntegrationTestBase;

@Transactional
class AuthControllerIT extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void cadastraFazLoginEConsultaPerfil() throws Exception {
		mockMvc.perform(post("/api/v1/auth/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "Ana Souza",
								  "cpf": "111.444.777-35",
								  "email": "Ana@Example.com",
								  "senha": "senha123",
								  "restricaoIds": [12]
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/usuarios/me"))
				.andExpect(jsonPath("$.nome").value("Ana Souza"))
				.andExpect(jsonPath("$.cpf").value("11144477735"))
				.andExpect(jsonPath("$.email").value("ana@example.com"))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist())
				.andExpect(jsonPath("$.restricoes[0].nome").value("Vegano"));

		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "ana@example.com", "senha": "senha123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.expiraEm").isNotEmpty())
				.andReturn();

		String token = extrairToken(login.getResponse().getContentAsString());

		mockMvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("ana@example.com"))
				.andExpect(jsonPath("$.restricoes[0].nome").value("Vegano"));

		mockMvc.perform(put("/api/v1/usuarios/me")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Ana Clara", "restricaoIds": []}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Ana Clara"))
				.andExpect(jsonPath("$.restricoes").isEmpty());
	}

	@Test
	void cadastroComCpfInvalidoRetorna400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "Ana",
								  "cpf": "111.111.111-11",
								  "email": "ana@example.com",
								  "senha": "senha123"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("cpf"));
	}

	@Test
	void cadastroDuplicadoRetorna409() throws Exception {
		String corpo = """
				{
				  "nome": "Ana",
				  "cpf": "11144477735",
				  "email": "ana@example.com",
				  "senha": "senha123"
				}
				""";
		mockMvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isConflict());
	}

	@Test
	void loginInvalidoRetorna401() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "naoexiste@example.com", "senha": "senha123"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Não autenticado"));
	}

	@Test
	void perfilSemTokenRetorna401() throws Exception {
		mockMvc.perform(get("/api/v1/usuarios/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Não autenticado"));
	}

	private static String extrairToken(String json) {
		String chave = "\"token\":\"";
		int inicio = json.indexOf(chave);
		if (inicio < 0) {
			throw new IllegalStateException("Token não encontrado na resposta: " + json);
		}
		int valor = inicio + chave.length();
		return json.substring(valor, json.indexOf('"', valor));
	}

}
