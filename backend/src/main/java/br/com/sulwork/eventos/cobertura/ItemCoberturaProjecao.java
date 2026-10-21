package br.com.sulwork.eventos.cobertura;

public interface ItemCoberturaProjecao {

	Long getItemId();

	String getNome();

	Long getCategoriaId();

	String getCategoriaNome();

	boolean getObrigatorio();

	int getQuantidadeNecessaria();

	int getContribuido();

}
