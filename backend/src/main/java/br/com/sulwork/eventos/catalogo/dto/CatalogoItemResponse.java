package br.com.sulwork.eventos.catalogo.dto;

import br.com.sulwork.eventos.catalogo.Alergeno;
import br.com.sulwork.eventos.catalogo.Categoria;

public record CatalogoItemResponse(Long id, String nome) {

	public static CatalogoItemResponse de(Categoria categoria) {
		return new CatalogoItemResponse(categoria.getId(), categoria.getNome());
	}

	public static CatalogoItemResponse de(Alergeno alergeno) {
		return new CatalogoItemResponse(alergeno.getId(), alergeno.getNome());
	}

}
