package br.com.sulwork.eventos.item.dto;

import java.util.List;

import br.com.sulwork.eventos.catalogo.dto.CatalogoItemResponse;
import br.com.sulwork.eventos.item.Item;

public record ItemResponse(
		Long id,
		String nome,
		CatalogoItemResponse categoria,
		int quantidadeNecessaria,
		boolean obrigatorio,
		List<CatalogoItemResponse> alergenos) {

	public static ItemResponse de(Item item) {
		return new ItemResponse(
				item.getId(),
				item.getNome(),
				CatalogoItemResponse.de(item.getCategoria()),
				item.getQuantidadeNecessaria(),
				item.isObrigatorio(),
				item.getAlergenos().stream().map(CatalogoItemResponse::de).toList());
	}

}
