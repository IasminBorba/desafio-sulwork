package br.com.sulwork.eventos.cobertura;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.sulwork.eventos.cobertura.dto.CoberturaCategoriaResponse;
import br.com.sulwork.eventos.cobertura.dto.CoberturaItemResponse;
import br.com.sulwork.eventos.cobertura.dto.CoberturaResponse;

@Component
public class CalculadoraCobertura {

	public CoberturaResponse calcular(List<ItemCoberturaDados> itens, List<MetaCoberturaDados> metas) {
		if (itens.isEmpty()) {
			return new CoberturaResponse(0, 0, null, null, List.of(), List.of(), List.of());
		}

		double a = calcularA(itens);
		Double b = metas.isEmpty() ? null : calcularB(itens, metas);
		boolean temObrigatorio = itens.stream().anyMatch(ItemCoberturaDados::obrigatorio);
		Double c = temObrigatorio ? calcularC(itens) : null;
		double geral = combinar(a, b, c);

		List<CoberturaItemResponse> itensResposta = itens.stream().map(this::item).toList();
		List<CoberturaCategoriaResponse> categorias = metas.stream()
				.map(meta -> categoria(meta, contribuidoNaCategoria(itens, meta.categoriaId())))
				.toList();
		List<CoberturaItemResponse> pendentes = itensResposta.stream()
				.filter(item -> item.obrigatorio() && item.faltante() > 0)
				.toList();

		return new CoberturaResponse(percentual(geral), arredondar(a), nuloOuArredondado(b), nuloOuArredondado(c),
				categorias, itensResposta, pendentes);
	}

	private double calcularA(List<ItemCoberturaDados> itens) {
		int pesos = itens.stream().mapToInt(ItemCoberturaDados::peso).sum();
		double soma = itens.stream().mapToDouble(item -> item.peso() * item.coberturaItem()).sum();
		return pesos == 0 ? 0 : soma / pesos;
	}

	private double calcularB(List<ItemCoberturaDados> itens, List<MetaCoberturaDados> metas) {
		return metas.stream()
				.mapToDouble(meta -> {
					int contribuido = contribuidoNaCategoria(itens, meta.categoriaId());
					return Math.min(contribuido, meta.quantidadeMeta()) / (double) meta.quantidadeMeta();
				})
				.average()
				.orElse(0);
	}

	private double calcularC(List<ItemCoberturaDados> itens) {
		List<ItemCoberturaDados> obrigatorios = itens.stream().filter(ItemCoberturaDados::obrigatorio).toList();
		long completos = obrigatorios.stream().filter(ItemCoberturaDados::coberturaCompleta).count();
		return completos / (double) obrigatorios.size();
	}

	private static double combinar(double a, Double b, Double c) {
		if (b != null && c != null) {
			return 0.5 * a + 0.3 * b + 0.2 * c;
		}
		if (b == null && c != null) {
			return (0.5 * a + 0.2 * c) / 0.7;
		}
		if (b != null) {
			return (0.5 * a + 0.3 * b) / 0.8;
		}
		return a;
	}

	private CoberturaItemResponse item(ItemCoberturaDados item) {
		return new CoberturaItemResponse(
				item.itemId(),
				item.nome(),
				item.categoriaNome(),
				item.obrigatorio(),
				item.quantidadeNecessaria(),
				item.contribuido(),
				item.faltante(),
				percentual(item.coberturaItem()));
	}

	private CoberturaCategoriaResponse categoria(MetaCoberturaDados meta, int contribuido) {
		double cobertura = Math.min(contribuido, meta.quantidadeMeta()) / (double) meta.quantidadeMeta();
		return new CoberturaCategoriaResponse(
				meta.categoriaId(),
				meta.categoriaNome(),
				meta.quantidadeMeta(),
				contribuido,
				percentual(cobertura));
	}

	private static int contribuidoNaCategoria(List<ItemCoberturaDados> itens, Long categoriaId) {
		return itens.stream()
				.filter(item -> item.categoriaId().equals(categoriaId))
				.mapToInt(ItemCoberturaDados::contribuido)
				.sum();
	}

	private static int percentual(double valor) {
		return (int) Math.round(valor * 100);
	}

	private static double arredondar(double valor) {
		return Math.round(valor * 10000d) / 10000d;
	}

	private static Double nuloOuArredondado(Double valor) {
		return valor == null ? null : arredondar(valor);
	}

}
