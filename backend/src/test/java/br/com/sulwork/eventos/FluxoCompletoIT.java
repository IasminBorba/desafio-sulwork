package br.com.sulwork.eventos;

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
class FluxoCompletoIT extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Test
	void cadastroLoginEventoItemMetaPublicarParticiparContribuirECobertura() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEvento(ana);
		long categoriaId = categoriaId("Bebidas");

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem(categoriaId)))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/metas")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"categoriaId": %d, "quantidadeMeta": 12}
								""".formatted(categoriaId)))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/publicar").header("Authorization", bearer(ana)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ABERTO"));

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(bia)))
				.andExpect(status().isCreated());
		long itemId = extrairPrimeiroItemId(ana, eventoId);
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemId": %d, "quantidade": 4}
								""".formatted(itemId)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/cobertura").header("Authorization", bearer(bia)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itens[0].contribuido").value(4))
				.andExpect(jsonPath("$.itens[0].faltante").value(6));
	}

	@Test
	void semTokenRetorna401ERascunhoAlheioRetorna404() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEvento(ana);

		mockMvc.perform(get("/api/v1/eventos/" + eventoId))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/eventos/" + eventoId).header("Authorization", bearer(bia)))
				.andExpect(status().isNotFound());
	}

	@Test
	void naoOrganizadorNaoEditaItemDeEventoAberto() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEvento(ana);
		long categoriaId = categoriaId("Bebidas");
		MvcResult item = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem(categoriaId)))
				.andExpect(status().isCreated())
				.andReturn();
		long itemId = extrairId(item.getResponse().getContentAsString());
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/publicar").header("Authorization", bearer(ana)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem(categoriaId)))
				.andExpect(status().isForbidden());
		mockMvc.perform(put("/api/v1/eventos/" + eventoId + "/itens/" + itemId)
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem(categoriaId)))
				.andExpect(status().isForbidden());
	}

	private long criarEvento(String token) throws Exception {
		Instant agora = relogio.instant();
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "Churrasco",
								  "descricao": "Fluxo completo",
								  "tipo": "CHURRASCO",
								  "dataInicio": "%s",
								  "dataFim": "%s",
								  "prazoAlteracao": "%s"
								}
								""".formatted(
								agora.plus(Duration.ofDays(2)),
								agora.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
								agora.plus(Duration.ofDays(1)))))
				.andExpect(status().isCreated())
				.andReturn();
		return extrairId(criado.getResponse().getContentAsString());
	}

	private long extrairPrimeiroItemId(String token, long eventoId) throws Exception {
		MvcResult lista = mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andReturn();
		return extrairId(lista.getResponse().getContentAsString());
	}

	private long categoriaId(String nome) {
		return categoriaRepository.findAllByOrderByNomeAsc().stream()
				.filter(categoria -> categoria.getNome().equals(nome))
				.map(Categoria::getId)
				.findFirst()
				.orElseThrow();
	}

	private static String corpoItem(long categoriaId) {
		return """
				{
				  "nome": "Refrigerante 2L",
				  "categoriaId": %d,
				  "quantidadeNecessaria": 10,
				  "obrigatorio": true,
				  "alergenoIds": []
				}
				""".formatted(categoriaId);
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
