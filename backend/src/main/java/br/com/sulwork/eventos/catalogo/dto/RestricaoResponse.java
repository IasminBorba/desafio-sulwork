package br.com.sulwork.eventos.catalogo.dto;

import java.util.Comparator;
import java.util.List;

import br.com.sulwork.eventos.catalogo.RestricaoAlimentar;

public record RestricaoResponse(Long id, String nome, List<CatalogoItemResponse> alergenos) {

	public static RestricaoResponse de(RestricaoAlimentar restricao) {
		List<CatalogoItemResponse> alergenos = restricao.getAlergenos().stream()
				.sorted(Comparator.comparing(alergeno -> alergeno.getNome()))
				.map(CatalogoItemResponse::de)
				.toList();
		return new RestricaoResponse(restricao.getId(), restricao.getNome(), alergenos);
	}

}
