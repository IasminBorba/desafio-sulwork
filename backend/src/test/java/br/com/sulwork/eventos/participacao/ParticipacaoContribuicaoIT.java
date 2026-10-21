package br.com.sulwork.eventos.participacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.catalogo.CategoriaRepository;
import br.com.sulwork.eventos.support.AutenticacaoTeste;
import br.com.sulwork.eventos.support.IntegrationTestBase;

@Transactional
class ParticipacaoContribuicaoIT extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Test
	void participaContribuiERespeitaLimiteEPrazo() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEPublicar(ana);
		long itemId = criarItem(ana, eventoId, 10);

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 3)))
				.andExpect(status().isUnprocessableContent());

		MvcResult participacao = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(bia)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Bia"))
				.andExpect(jsonPath("$.cpf").value("529.***.***-25"))
				.andReturn();
		long participacaoId = extrairId(participacao.getResponse().getContentAsString());

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(bia)))
				.andExpect(status().isConflict());

		MvcResult contribuicao = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 6)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.quantidade").value(6))
				.andReturn();
		long contribuicaoId = extrairId(contribuicao.getResponse().getContentAsString());

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 1)))
				.andExpect(status().isConflict());

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(ana)))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 5)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Restam 4")));

		mockMvc.perform(put("/api/v1/eventos/" + eventoId + "/contribuicoes/" + contribuicaoId)
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 2)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(ana)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.nome=='Bia')].cpf").value(org.hamcrest.Matchers.hasItem("529.***.***-25")));

		relogio.definir(relogio.instant().plus(Duration.ofHours(2)).plusSeconds(1));
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 1)))
				.andExpect(status().isConflict());
		mockMvc.perform(delete("/api/v1/eventos/" + eventoId + "/participacoes/" + participacaoId)
						.header("Authorization", bearer(ana)))
				.andExpect(status().isConflict());
	}

	@Test
	void donoEditaERemoveAPropriaContribuicao() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEPublicar(ana);
		long itemId = criarItem(ana, eventoId, 10);
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(bia)))
				.andExpect(status().isCreated());
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 4)))
				.andExpect(status().isCreated())
				.andReturn();
		long contribuicaoId = extrairId(criado.getResponse().getContentAsString());

		mockMvc.perform(put("/api/v1/eventos/" + eventoId + "/contribuicoes/" + contribuicaoId)
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoContribuicao(itemId, 7)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantidade").value(7));

		mockMvc.perform(delete("/api/v1/eventos/" + eventoId + "/contribuicoes/" + contribuicaoId)
						.header("Authorization", bearer(bia)))
				.andExpect(status().isNoContent());
	}

	private long criarEPublicar(String token) throws Exception {
		Instant agora = relogio.instant();
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "Churrasco",
								  "descricao": "Equipe",
								  "tipo": "CHURRASCO",
								  "dataInicio": "%s",
								  "dataFim": "%s",
								  "prazoAlteracao": "%s"
								}
								""".formatted(
								agora.plus(Duration.ofHours(3)),
								agora.plus(Duration.ofHours(7)),
								agora.plus(Duration.ofHours(2)))))
				.andExpect(status().isCreated())
				.andReturn();
		long eventoId = extrairId(criado.getResponse().getContentAsString());
		return eventoId;
	}

	private long criarItem(String token, long eventoId, int quantidade) throws Exception {
		long categoriaId = categoriaRepository.findAllByOrderByNomeAsc().stream()
				.filter(categoria -> categoria.getNome().equals("Bebidas"))
				.map(Categoria::getId)
				.findFirst()
				.orElseThrow();
		MvcResult item = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "Refrigerante 2L",
								  "categoriaId": %d,
								  "quantidadeNecessaria": %d,
								  "obrigatorio": true,
								  "alergenoIds": []
								}
								""".formatted(categoriaId, quantidade)))
				.andExpect(status().isCreated())
				.andReturn();
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/publicar").header("Authorization", bearer(token)))
				.andExpect(status().isOk());
		return extrairId(item.getResponse().getContentAsString());
	}

	private static String corpoContribuicao(long itemId, int quantidade) {
		return """
				{"itemId": %d, "quantidade": %d}
				""".formatted(itemId, quantidade);
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

	private static long extrairId(String json) {
		String chave = "\"id\":";
		int inicio = json.indexOf(chave);
		int valor = inicio + chave.length();
		int fim = json.indexOf(',', valor);
		if (fim < 0) {
			fim = json.indexOf('}', valor);
		}
		return Long.parseLong(json.substring(valor, fim).strip());
	}

}
