package br.com.sulwork.eventos.meta.dto;

import br.com.sulwork.eventos.catalogo.dto.CatalogoItemResponse;
import br.com.sulwork.eventos.meta.MetaCategoria;

public record MetaResponse(Long id, CatalogoItemResponse categoria, int quantidadeMeta) {

	public static MetaResponse de(MetaCategoria meta) {
		return new MetaResponse(meta.getId(), CatalogoItemResponse.de(meta.getCategoria()), meta.getQuantidadeMeta());
	}

}
