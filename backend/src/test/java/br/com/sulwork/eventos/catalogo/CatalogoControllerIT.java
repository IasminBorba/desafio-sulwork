package br.com.sulwork.eventos.catalogo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sulwork.eventos.support.IntegrationTestBase;

class CatalogoControllerIT extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listaCategoriasSemAutenticacao() throws Exception {
		mockMvc.perform(get("/api/v1/categorias"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10))
				.andExpect(jsonPath("$[0].nome").value("Acompanhamentos"))
				.andExpect(jsonPath("$[9].nome").value("Salgados"));
	}

	@Test
	void listaAlergenosSemAutenticacao() throws Exception {
		mockMvc.perform(get("/api/v1/alergenos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(11))
				.andExpect(jsonPath("$[?(@.nome == 'Carne')]").exists())
				.andExpect(jsonPath("$[?(@.nome == 'Mel')]").exists())
				.andExpect(jsonPath("$[?(@.nome == 'Glúten')]").exists());
	}

	@Test
	void listaRestricoesComAlergenosRelacionados() throws Exception {
		mockMvc.perform(get("/api/v1/restricoes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(12))
				.andExpect(jsonPath("$[?(@.nome == 'Vegano')].alergenos.length()").value(6))
				.andExpect(jsonPath("$[?(@.nome == 'Vegano')].alergenos[*].nome")
						.value(org.hamcrest.Matchers.containsInAnyOrder(
								"Carne", "Crustáceos", "Leite e derivados", "Mel", "Ovo", "Peixe")))
				.andExpect(jsonPath("$[?(@.nome == 'Vegetariano')].alergenos.length()").value(3));
	}

}
