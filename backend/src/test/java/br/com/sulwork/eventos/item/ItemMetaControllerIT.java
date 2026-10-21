package br.com.sulwork.eventos.item;

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
class ItemMetaControllerIT extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Test
	void organizadorCriaEditaERemoveItemEMeta() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		long eventoId = criarEvento(ana, "Churrasco");
		long bebidasId = categoriaId("Bebidas");

		MvcResult itemCriado = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem("Refrigerante 2L", bebidasId, 10, true)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Refrigerante 2L"))
				.andExpect(jsonPath("$.obrigatorio").value(true))
				.andReturn();
		long itemId = extrairId(itemCriado.getResponse().getContentAsString());

		MvcResult metaCriada = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/metas")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoMeta(bebidasId, 12)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.quantidadeMeta").value(12))
				.andReturn();
		long metaId = extrairId(metaCriada.getResponse().getContentAsString());

		mockMvc.perform(put("/api/v1/eventos/" + eventoId + "/itens/" + itemId)
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem("Refrigerante 2L", bebidasId, 8, true)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantidadeNecessaria").value(8));

		mockMvc.perform(put("/api/v1/eventos/" + eventoId + "/metas/" + metaId)
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoMeta(bebidasId, 15)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantidadeMeta").value(15));

		mockMvc.perform(delete("/api/v1/eventos/" + eventoId + "/itens/" + itemId)
						.header("Authorization", bearer(ana)))
				.andExpect(status().isConflict());

		mockMvc.perform(delete("/api/v1/eventos/" + eventoId + "/metas/" + metaId)
						.header("Authorization", bearer(ana)))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/v1/eventos/" + eventoId + "/itens/" + itemId)
						.header("Authorization", bearer(ana)))
				.andExpect(status().isNoContent());
	}

	@Test
	void metaSemItemDaCategoriaRetorna422() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		long eventoId = criarEvento(ana, "Churrasco");
		long bebidasId = categoriaId("Bebidas");

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/metas")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoMeta(bebidasId, 12)))
				.andExpect(status().isUnprocessableContent());
	}

	@Test
	void naoOrganizadorNaoGerenciaItens() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEvento(ana, "Churrasco");
		long bebidasId = categoriaId("Bebidas");

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem("Refrigerante", bebidasId, 10, true)))
				.andExpect(status().isNotFound());
	}

	@Test
	void quantidadeZeroRetorna400() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		long eventoId = criarEvento(ana, "Churrasco");
		long bebidasId = categoriaId("Bebidas");

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem("Refrigerante", bebidasId, 0, true)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void participanteVeItensDeEventoAberto() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEvento(ana, "Churrasco");
		long bebidasId = categoriaId("Bebidas");
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(ana))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoItem("Refrigerante", bebidasId, 10, true)))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/publicar").header("Authorization", bearer(ana)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/itens").header("Authorization", bearer(bia)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nome").value("Refrigerante"));
	}

	private long criarEvento(String token, String nome) throws Exception {
		Instant agora = relogio.instant();
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "%s",
								  "descricao": "Celebração da equipe",
								  "tipo": "CHURRASCO",
								  "dataInicio": "%s",
								  "dataFim": "%s",
								  "prazoAlteracao": "%s"
								}
								""".formatted(
								nome,
								agora.plus(Duration.ofDays(2)),
								agora.plus(Duration.ofDays(2)).plus(Duration.ofHours(4)),
								agora.plus(Duration.ofDays(1)))))
				.andExpect(status().isCreated())
				.andReturn();
		return extrairId(criado.getResponse().getContentAsString());
	}

	private long categoriaId(String nome) {
		return categoriaRepository.findAllByOrderByNomeAsc().stream()
				.filter(categoria -> categoria.getNome().equals(nome))
				.map(Categoria::getId)
				.findFirst()
				.orElseThrow();
	}

	private static String corpoItem(String nome, long categoriaId, int quantidade, boolean obrigatorio) {
		return """
				{
				  "nome": "%s",
				  "categoriaId": %d,
				  "quantidadeNecessaria": %d,
				  "obrigatorio": %s,
				  "alergenoIds": []
				}
				""".formatted(nome, categoriaId, quantidade, obrigatorio);
	}

	private static String corpoMeta(long categoriaId, int quantidade) {
		return """
				{"categoriaId": %d, "quantidadeMeta": %d}
				""".formatted(categoriaId, quantidade);
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
