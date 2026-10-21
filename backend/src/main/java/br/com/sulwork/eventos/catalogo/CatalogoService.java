package br.com.sulwork.eventos.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.catalogo.dto.CatalogoItemResponse;
import br.com.sulwork.eventos.catalogo.dto.RestricaoResponse;

@Service
public class CatalogoService {

	private final CategoriaRepository categoriaRepository;
	private final AlergenoRepository alergenoRepository;
	private final RestricaoAlimentarRepository restricaoAlimentarRepository;

	public CatalogoService(CategoriaRepository categoriaRepository, AlergenoRepository alergenoRepository,
			RestricaoAlimentarRepository restricaoAlimentarRepository) {
		this.categoriaRepository = categoriaRepository;
		this.alergenoRepository = alergenoRepository;
		this.restricaoAlimentarRepository = restricaoAlimentarRepository;
	}

	@Transactional(readOnly = true)
	public List<CatalogoItemResponse> categorias() {
		return categoriaRepository.findAllByOrderByNomeAsc().stream()
				.map(CatalogoItemResponse::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CatalogoItemResponse> alergenos() {
		return alergenoRepository.findAllByOrderByNomeAsc().stream()
				.map(CatalogoItemResponse::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<RestricaoResponse> restricoes() {
		return restricaoAlimentarRepository.findAllByOrderByNomeAsc().stream()
				.map(RestricaoResponse::de)
				.toList();
	}

}
