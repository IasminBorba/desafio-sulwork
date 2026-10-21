package br.com.sulwork.eventos.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public final class AutenticacaoTeste {

	private AutenticacaoTeste() {
	}

	public static String cadastrarELogar(MockMvc mockMvc, String nome, String cpf, String email) throws Exception {
		mockMvc.perform(post("/api/v1/auth/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "%s",
								  "cpf": "%s",
								  "email": "%s",
								  "senha": "senha123"
								}
								""".formatted(nome, cpf, email)))
				.andExpect(status().isCreated());
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "%s", "senha": "senha123"}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andReturn();
		return extrairToken(login.getResponse().getContentAsString());
	}

	public static String extrairToken(String json) {
		String chave = "\"token\":\"";
		int inicio = json.indexOf(chave);
		if (inicio < 0) {
			throw new IllegalStateException("Token não encontrado na resposta: " + json);
		}
		int valor = inicio + chave.length();
		return json.substring(valor, json.indexOf('"', valor));
	}

}
