package br.com.sulwork.eventos.cobertura;

public record ItemCoberturaDados(
		Long itemId,
		String nome,
		Long categoriaId,
		String categoriaNome,
		boolean obrigatorio,
		int quantidadeNecessaria,
		int contribuido) {

	public static ItemCoberturaDados de(ItemCoberturaProjecao linha) {
		return new ItemCoberturaDados(
				linha.getItemId(),
				linha.getNome(),
				linha.getCategoriaId(),
				linha.getCategoriaNome(),
				linha.getObrigatorio(),
				linha.getQuantidadeNecessaria(),
				linha.getContribuido());
	}

	public double coberturaItem() {
		if (quantidadeNecessaria <= 0) {
			return 0;
		}
		return Math.min(contribuido, quantidadeNecessaria) / (double) quantidadeNecessaria;
	}

	public int peso() {
		return obrigatorio ? 2 : 1;
	}

	public int faltante() {
		return Math.max(0, quantidadeNecessaria - contribuido);
	}

	public boolean coberturaCompleta() {
		return contribuido >= quantidadeNecessaria;
	}

}
