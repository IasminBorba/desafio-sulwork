package br.com.sulwork.eventos.cobertura;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.catalogo.Alergeno;
import br.com.sulwork.eventos.catalogo.AlergenoRepository;
import br.com.sulwork.eventos.catalogo.Categoria;
import br.com.sulwork.eventos.catalogo.CategoriaRepository;
import br.com.sulwork.eventos.catalogo.RestricaoAlimentar;
import br.com.sulwork.eventos.catalogo.RestricaoAlimentarRepository;
import br.com.sulwork.eventos.support.AutenticacaoTeste;
import br.com.sulwork.eventos.support.IntegrationTestBase;

@Transactional
class CoberturaPainelIT extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private AlergenoRepository alergenoRepository;

	@Autowired
	private RestricaoAlimentarRepository restricaoAlimentarRepository;

	@Test
	void calculaOExemploDe61PorCentoEMontaOPainel() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long eventoId = criarEvento(ana);
		long refrigerante = criarItem(ana, eventoId, "Refrigerante 2L", "Bebidas", 10, true, List.of());
		long bolo = criarItem(ana, eventoId, "Bolo", "Doces e sobremesas", 2, true, List.of());
		criarItem(ana, eventoId, "Guardanapo", "Descartáveis e utensílios", 100, false, List.of());
		criarMeta(ana, eventoId, "Bebidas", 12);
		criarMeta(ana, eventoId, "Doces e sobremesas", 2);
		publicar(ana, eventoId);

		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(ana)))
				.andExpect(status().isCreated());
		contribuir(ana, eventoId, refrigerante, 5);
		contribuir(ana, eventoId, bolo, 2);
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(bia)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/cobertura")
						.header("Authorization", bearer(ana)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.percentualGeral").value(61))
				.andExpect(jsonPath("$.a").value(0.6))
				.andExpect(jsonPath("$.c").value(0.5))
				.andExpect(jsonPath("$.obrigatoriosPendentes[0].nome").value("Refrigerante 2L"));

		mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/painel")
						.header("Authorization", bearer(bia)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/painel")
						.header("Authorization", bearer(ana)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cobertura.percentualGeral").value(61))
				.andExpect(jsonPath("$.participantes[?(@.nome=='Bia')].semContribuicao")
						.value(org.hamcrest.Matchers.hasItem(true)))
				.andExpect(jsonPath("$.participantes[?(@.nome=='Ana')].contribuicoes.length()")
						.value(org.hamcrest.Matchers.hasItem(2)));
	}

	@Test
	void apontaConflitoDeRestricaoComAlergenoDoItem() throws Exception {
		long celíaca = restricaoAlimentarRepository.findAllByOrderByNomeAsc().stream()
				.filter(restricao -> restricao.getNome().startsWith("Doença celíaca"))
				.map(RestricaoAlimentar::getId)
				.findFirst()
				.orElseThrow();
		long gluten = alergenoRepository.findAllByOrderByNomeAsc().stream()
				.filter(alergeno -> alergeno.getNome().equals("Glúten"))
				.map(Alergeno::getId)
				.findFirst()
				.orElseThrow();
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com",
				List.of(celíaca));
		long eventoId = criarEvento(ana);
		criarItem(ana, eventoId, "Pão", "Pães e frios", 10, true, List.of(gluten));
		publicar(ana, eventoId);
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/participacoes")
						.header("Authorization", bearer(bia)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/eventos/" + eventoId + "/restricoes")
						.header("Authorization", bearer(ana)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resumo[0].quantidadeParticipantes").value(1))
				.andExpect(jsonPath("$.porItem[0].quantidadeAfetados").value(1))
				.andExpect(jsonPath("$.porItem[0].participantes[0].nome").value("Bia"))
				.andExpect(jsonPath("$.semItemCompativel[0].nome").value(org.hamcrest.Matchers.startsWith("Doença celíaca")));
	}

	private long criarEvento(String token) throws Exception {
		Instant agora = relogio.instant();
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "Almoço da equipe",
								  "descricao": "Cobertura",
								  "tipo": "ALMOCO",
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

	private long criarItem(String token, long eventoId, String nome, String categoria, int quantidade,
			boolean obrigatorio, List<Long> alergenos) throws Exception {
		long categoriaId = categoriaRepository.findAllByOrderByNomeAsc().stream()
				.filter(item -> item.getNome().equals(categoria))
				.map(Categoria::getId)
				.findFirst()
				.orElseThrow();
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/itens")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nome": "%s",
								  "categoriaId": %d,
								  "quantidadeNecessaria": %d,
								  "obrigatorio": %s,
								  "alergenoIds": %s
								}
								""".formatted(nome, categoriaId, quantidade, obrigatorio, alergenos)))
				.andExpect(status().isCreated())
				.andReturn();
		return extrairId(criado.getResponse().getContentAsString());
	}

	private void criarMeta(String token, long eventoId, String categoria, int quantidade) throws Exception {
		long categoriaId = categoriaRepository.findAllByOrderByNomeAsc().stream()
				.filter(item -> item.getNome().equals(categoria))
				.map(Categoria::getId)
				.findFirst()
				.orElseThrow();
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/metas")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"categoriaId": %d, "quantidadeMeta": %d}
								""".formatted(categoriaId, quantidade)))
				.andExpect(status().isCreated());
	}

	private void publicar(String token, long eventoId) throws Exception {
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/publicar").header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	private void contribuir(String token, long eventoId, long itemId, int quantidade) throws Exception {
		mockMvc.perform(post("/api/v1/eventos/" + eventoId + "/contribuicoes")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemId": %d, "quantidade": %d}
								""".formatted(itemId, quantidade)))
				.andExpect(status().isCreated());
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
