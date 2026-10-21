package br.com.sulwork.eventos.cobertura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.sulwork.eventos.cobertura.dto.CoberturaResponse;

class CalculadoraCoberturaTest {

	private final CalculadoraCobertura calculadora = new CalculadoraCobertura();

	@Test
	void exemploDaDocumentacaoDa61PorCento() {
		List<ItemCoberturaDados> itens = List.of(
				new ItemCoberturaDados(1L, "Refrigerante 2L", 1L, "Bebidas", true, 10, 5),
				new ItemCoberturaDados(2L, "Bolo", 2L, "Doces", true, 2, 2),
				new ItemCoberturaDados(3L, "Guardanapo", 3L, "Descartáveis", false, 100, 0));
		List<MetaCoberturaDados> metas = List.of(
				new MetaCoberturaDados(1L, "Bebidas", 12),
				new MetaCoberturaDados(2L, "Doces", 2));

		CoberturaResponse resposta = calculadora.calcular(itens, metas);

		assertThat(resposta.a()).isEqualTo(0.6);
		assertThat(resposta.b()).isCloseTo(0.7083, within(0.0001));
		assertThat(resposta.c()).isEqualTo(0.5);
		assertThat(resposta.percentualGeral()).isEqualTo(61);
		assertThat(resposta.obrigatoriosPendentes()).extracting(item -> item.nome())
				.containsExactly("Refrigerante 2L");
	}

	@Test
	void semMetasRenormalizaOsPesos() {
		List<ItemCoberturaDados> itens = List.of(
				new ItemCoberturaDados(1L, "Bolo", 2L, "Doces", true, 2, 2),
				new ItemCoberturaDados(2L, "Guardanapo", 3L, "Descartáveis", false, 100, 0));

		CoberturaResponse resposta = calculadora.calcular(itens, List.of());

		assertThat(resposta.b()).isNull();
		assertThat(resposta.percentualGeral()).isEqualTo((int) Math.round(((0.5 * resposta.a() + 0.2 * resposta.c()) / 0.7) * 100));
	}

	@Test
	void semObrigatoriosRenormalizaOsPesos() {
		List<ItemCoberturaDados> itens = List.of(
				new ItemCoberturaDados(1L, "Guardanapo", 3L, "Descartáveis", false, 100, 50));
		List<MetaCoberturaDados> metas = List.of(new MetaCoberturaDados(3L, "Descartáveis", 80));

		CoberturaResponse resposta = calculadora.calcular(itens, metas);

		assertThat(resposta.c()).isNull();
		assertThat(resposta.a()).isEqualTo(0.5);
		assertThat(resposta.percentualGeral()).isEqualTo(
				(int) Math.round(((0.5 * 0.5 + 0.3 * (50 / 80d)) / 0.8) * 100));
	}

	@Test
	void semMetasESemObrigatoriosUsaSoA() {
		List<ItemCoberturaDados> itens = List.of(
				new ItemCoberturaDados(1L, "Guardanapo", 3L, "Descartáveis", false, 100, 50));

		CoberturaResponse resposta = calculadora.calcular(itens, List.of());

		assertThat(resposta.b()).isNull();
		assertThat(resposta.c()).isNull();
		assertThat(resposta.percentualGeral()).isEqualTo(50);
	}

	@Test
	void semItensRetornaZero() {
		CoberturaResponse resposta = calculadora.calcular(List.of(), List.of());

		assertThat(resposta.percentualGeral()).isEqualTo(0);
		assertThat(resposta.itens()).isEmpty();
	}

	@Test
	void coberturaTotalDaCemPorCento() {
		List<ItemCoberturaDados> itens = List.of(
				new ItemCoberturaDados(1L, "Bolo", 2L, "Doces", true, 2, 2));
		List<MetaCoberturaDados> metas = List.of(new MetaCoberturaDados(2L, "Doces", 2));

		CoberturaResponse resposta = calculadora.calcular(itens, metas);

		assertThat(resposta.percentualGeral()).isEqualTo(100);
		assertThat(resposta.obrigatoriosPendentes()).isEmpty();
	}

}
