package br.com.sulwork.eventos.catalogo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.com.sulwork.eventos.shared.exception.RegraNegocioException;

@Component
public class RestricaoCatalogo {

	private final RestricaoAlimentarRepository restricaoAlimentarRepository;

	public RestricaoCatalogo(RestricaoAlimentarRepository restricaoAlimentarRepository) {
		this.restricaoAlimentarRepository = restricaoAlimentarRepository;
	}

	public Set<RestricaoAlimentar> buscar(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Set.of();
		}
		List<Long> distintos = ids.stream().distinct().toList();
		List<RestricaoAlimentar> encontradas = restricaoAlimentarRepository.findAllById(distintos);
		if (encontradas.size() != distintos.size()) {
			throw new RegraNegocioException("Uma ou mais restrições alimentares não existem.");
		}
		return new LinkedHashSet<>(encontradas);
	}

}
