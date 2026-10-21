package br.com.sulwork.eventos.cobertura;

public record MetaCoberturaDados(Long categoriaId, String categoriaNome, int quantidadeMeta) {

	public static MetaCoberturaDados de(MetaCoberturaProjecao linha) {
		return new MetaCoberturaDados(linha.getCategoriaId(), linha.getCategoriaNome(), linha.getQuantidadeMeta());
	}

}
