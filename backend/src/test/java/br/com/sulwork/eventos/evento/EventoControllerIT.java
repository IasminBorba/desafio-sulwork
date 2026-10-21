package br.com.sulwork.eventos.evento;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import br.com.sulwork.eventos.catalogo.CategoriaRepository;
import br.com.sulwork.eventos.item.Item;
import br.com.sulwork.eventos.item.ItemRepository;
import br.com.sulwork.eventos.support.AutenticacaoTeste;
import br.com.sulwork.eventos.support.IntegrationTestBase;

@Transactional
class EventoControllerIT extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private EventoRepository eventoRepository;

	@Test
	void criaListaEditaEExcluiRascunho() throws Exception {
		String token = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String corpo = corpoEvento("Churrasco da equipe", relogio.instant());

		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpo))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/v1/eventos/")))
				.andExpect(jsonPath("$.status").value("RASCUNHO"))
				.andExpect(jsonPath("$.souOrganizador").value(true))
				.andExpect(jsonPath("$.aceitaAlteracoes").value(false))
				.andReturn();
		long id = extrairId(criado.getResponse().getContentAsString());

		mockMvc.perform(get("/api/v1/eventos").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].id", hasItem((int) id)))
				.andExpect(jsonPath("$.content[?(@.id == %d)].nome".formatted(id), hasItem("Churrasco da equipe")))
				.andExpect(jsonPath("$.content[?(@.id == %d)].souOrganizador".formatted(id), hasItem(true)));

		mockMvc.perform(put("/api/v1/eventos/" + id)
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoEvento("Churrasco da firma", relogio.instant())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Churrasco da firma"));

		mockMvc.perform(delete("/api/v1/eventos/" + id).header("Authorization", bearer(token)))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/v1/eventos/" + id).header("Authorization", bearer(token)))
				.andExpect(status().isNotFound());
	}

	@Test
	void ordenacaoInexistenteRetorna400() throws Exception {
		String token = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");

		mockMvc.perform(get("/api/v1/eventos")
						.param("sort", "[\"string\"]")
						.header("Authorization", bearer(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("ordenação")));
	}

	@Test
	void rascunhoEInvisivelParaQuemNaoOrganiza() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long id = criarEvento(ana, "Rascunho da Ana");

		mockMvc.perform(get("/api/v1/eventos/" + id).header("Authorization", bearer(bia)))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/v1/eventos").header("Authorization", bearer(bia)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].id", not(hasItem((int) id))));
		mockMvc.perform(put("/api/v1/eventos/" + id)
						.header("Authorization", bearer(bia))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoEvento("Hack", relogio.instant())))
				.andExpect(status().isNotFound());
	}

	@Test
	void eventoAbertoApareceParaColaboradorQueAindaNaoParticipa() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long id = criarEvento(ana, "Churrasco da equipe");
		adicionarItem(id);
		mockMvc.perform(post("/api/v1/eventos/" + id + "/publicar").header("Authorization", bearer(ana)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/eventos").header("Authorization", bearer(bia)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].id", hasItem((int) id)))
				.andExpect(jsonPath("$.content[?(@.id == %d)].status".formatted(id), hasItem("ABERTO")))
				.andExpect(jsonPath("$.content[?(@.id == %d)].souOrganizador".formatted(id), hasItem(false)));
		mockMvc.perform(get("/api/v1/eventos/" + id).header("Authorization", bearer(bia)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));
	}

	@Test
	void publicarSemItemRetorna422EComItemAbreOEvento() throws Exception {
		String token = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		long id = criarEvento(token, "Churrasco");

		mockMvc.perform(post("/api/v1/eventos/" + id + "/publicar").header("Authorization", bearer(token)))
				.andExpect(status().isUnprocessableContent());

		adicionarItem(id);
		mockMvc.perform(post("/api/v1/eventos/" + id + "/publicar").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ABERTO"))
				.andExpect(jsonPath("$.aceitaAlteracoes").value(true));
	}

	@Test
	void naoOrganizadorNaoPublicaEventoAberto() throws Exception {
		String ana = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		String bia = AutenticacaoTeste.cadastrarELogar(mockMvc, "Bia", "52998224725", "bia@example.com");
		long id = criarEvento(ana, "Churrasco");
		adicionarItem(id);
		mockMvc.perform(post("/api/v1/eventos/" + id + "/publicar").header("Authorization", bearer(ana)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/eventos/" + id + "/encerrar").header("Authorization", bearer(bia)))
				.andExpect(status().isForbidden());
	}

	@Test
	void reabreComNovoPrazoEnquantoADataDeInicioNaoPassou() throws Exception {
		String token = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		long id = criarEvento(token, "Churrasco");
		adicionarItem(id);
		mockMvc.perform(post("/api/v1/eventos/" + id + "/publicar").header("Authorization", bearer(token)))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/eventos/" + id + "/encerrar").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ENCERRADO"));

		Instant novoPrazo = relogio.instant().plus(Duration.ofHours(12));
		mockMvc.perform(post("/api/v1/eventos/" + id + "/reabrir")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"prazoAlteracao": "%s"}
								""".formatted(novoPrazo)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ABERTO"));
	}

	@Test
	void encerramentoLazyUmMinutoDepoisDoPrazo() throws Exception {
		String token = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		long id = criarEPublicarComPrazoAsDez(token);

		relogio.definir(Instant.parse("2026-06-15T13:01:00Z"));
		mockMvc.perform(get("/api/v1/eventos/" + id).header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ENCERRADO"))
				.andExpect(jsonPath("$.aceitaAlteracoes").value(false));
	}

	@Test
	void noInstanteExatoDoPrazoContinuaAberto() throws Exception {
		String token = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		long id = criarEPublicarComPrazoAsDez(token);

		relogio.definir(Instant.parse("2026-06-15T13:00:00Z"));
		mockMvc.perform(get("/api/v1/eventos/" + id).header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ABERTO"));
	}

	@Test
	void rascunhoComPrazoVencidoNaoEEncerrado() throws Exception {
		String token = AutenticacaoTeste.cadastrarELogar(mockMvc, "Ana", "11144477735", "ana@example.com");
		Instant prazo = Instant.parse("2026-06-15T13:00:00Z");
		String corpo = """
				{
				  "nome": "Rascunho atrasado",
				  "descricao": "Ainda montando",
				  "tipo": "CHURRASCO",
				  "dataInicio": "%s",
				  "dataFim": "%s",
				  "prazoAlteracao": "%s"
				}
				""".formatted(prazo.plus(Duration.ofHours(2)), prazo.plus(Duration.ofHours(6)), prazo);
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpo))
				.andExpect(status().isCreated())
				.andReturn();
		long id = extrairId(criado.getResponse().getContentAsString());

		relogio.definir(Instant.parse("2026-06-15T13:01:00Z"));
		mockMvc.perform(get("/api/v1/eventos/" + id).header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("RASCUNHO"));
	}

	private long criarEPublicarComPrazoAsDez(String token) throws Exception {
		Instant prazo = Instant.parse("2026-06-15T13:00:00Z");
		String corpo = """
				{
				  "nome": "Almoço das 10",
				  "descricao": "Prazo ao meio-dia UTC-3",
				  "tipo": "ALMOCO",
				  "dataInicio": "%s",
				  "dataFim": "%s",
				  "prazoAlteracao": "%s"
				}
				""".formatted(prazo.plus(Duration.ofHours(2)), prazo.plus(Duration.ofHours(6)), prazo);
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpo))
				.andExpect(status().isCreated())
				.andReturn();
		long id = extrairId(criado.getResponse().getContentAsString());
		adicionarItem(id);
		mockMvc.perform(post("/api/v1/eventos/" + id + "/publicar").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ABERTO"));
		return id;
	}

	private long criarEvento(String token, String nome) throws Exception {
		MvcResult criado = mockMvc.perform(post("/api/v1/eventos")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoEvento(nome, relogio.instant())))
				.andExpect(status().isCreated())
				.andReturn();
		return extrairId(criado.getResponse().getContentAsString());
	}

	private void adicionarItem(long eventoId) {
		var evento = eventoRepository.findById(eventoId).orElseThrow();
		var categoria = categoriaRepository.findAllByOrderByNomeAsc().getFirst();
		itemRepository.saveAndFlush(Item.novo(evento, categoria, "Picanha", 10, true));
	}

	private static String corpoEvento(String nome, Instant agora) {
		return """
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
				agora.plus(Duration.ofDays(1)));
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

	private static long extrairId(String json) {
		String chave = "\"id\":";
		int inicio = json.indexOf(chave);
		int valor = inicio + chave.length();
		int fim = json.indexOf(',', valor);
		return Long.parseLong(json.substring(valor, fim).strip());
	}

}
