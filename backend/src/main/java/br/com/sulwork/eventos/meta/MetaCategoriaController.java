package br.com.sulwork.eventos.meta;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.meta.dto.MetaRequest;
import br.com.sulwork.eventos.meta.dto.MetaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/metas")
@Tag(name = "Metas")
public class MetaCategoriaController {

	private final MetaCategoriaService metaCategoriaService;

	public MetaCategoriaController(MetaCategoriaService metaCategoriaService) {
		this.metaCategoriaService = metaCategoriaService;
	}

	@GetMapping
	@Operation(summary = "Listar metas por categoria")
	public List<MetaResponse> listar(@PathVariable Long eventoId) {
		return metaCategoriaService.listar(eventoId);
	}

	@PostMapping
	@Operation(summary = "Definir meta de uma categoria")
	public ResponseEntity<MetaResponse> criar(@PathVariable Long eventoId, @Valid @RequestBody MetaRequest request) {
		MetaResponse resposta = metaCategoriaService.criar(eventoId, request);
		return ResponseEntity.created(URI.create("/api/v1/eventos/" + eventoId + "/metas/" + resposta.id()))
				.body(resposta);
	}

	@PutMapping("/{metaId}")
	@Operation(summary = "Alterar meta de uma categoria")
	public MetaResponse atualizar(@PathVariable Long eventoId, @PathVariable Long metaId,
			@Valid @RequestBody MetaRequest request) {
		return metaCategoriaService.atualizar(eventoId, metaId, request);
	}

	@DeleteMapping("/{metaId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remover meta de uma categoria")
	public void excluir(@PathVariable Long eventoId, @PathVariable Long metaId) {
		metaCategoriaService.excluir(eventoId, metaId);
	}

}
