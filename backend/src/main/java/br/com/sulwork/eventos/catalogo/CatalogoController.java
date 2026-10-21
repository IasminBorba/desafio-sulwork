package br.com.sulwork.eventos.catalogo;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.catalogo.dto.CatalogoItemResponse;
import br.com.sulwork.eventos.catalogo.dto.RestricaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catálogos")
@SecurityRequirements
public class CatalogoController {

	private final CatalogoService catalogoService;

	public CatalogoController(CatalogoService catalogoService) {
		this.catalogoService = catalogoService;
	}

	@GetMapping("/categorias")
	@Operation(summary = "Listar categorias de itens")
	public List<CatalogoItemResponse> categorias() {
		return catalogoService.categorias();
	}

	@GetMapping("/alergenos")
	@Operation(summary = "Listar alérgenos e componentes")
	public List<CatalogoItemResponse> alergenos() {
		return catalogoService.alergenos();
	}

	@GetMapping("/restricoes")
	@Operation(summary = "Listar restrições alimentares com os alérgenos que evitam")
	public List<RestricaoResponse> restricoes() {
		return catalogoService.restricoes();
	}

}
