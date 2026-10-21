package br.com.sulwork.eventos.evento;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.evento.dto.EventoDetalheResponse;
import br.com.sulwork.eventos.evento.dto.EventoRequest;
import br.com.sulwork.eventos.evento.dto.EventoResumoResponse;
import br.com.sulwork.eventos.evento.dto.ReabrirRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Eventos")
public class EventoController {

	private final EventoService eventoService;

	public EventoController(EventoService eventoService) {
		this.eventoService = eventoService;
	}

	@GetMapping
	@Operation(summary = "Listar eventos do usuário")
	@Parameters({
			@Parameter(name = "page", description = "Página (começa em 0)", example = "0"),
			@Parameter(name = "size", description = "Quantidade por página", example = "20"),
			@Parameter(name = "sort", description = "Campo e direção, por exemplo dataInicio,desc",
					example = "dataInicio,desc")
	})
	public Page<EventoResumoResponse> listar(
			@RequestParam(required = false) StatusEvento status,
			@RequestParam(required = false) PapelEvento papel,
			@Parameter(hidden = true)
			@PageableDefault(size = 20, sort = "dataInicio", direction = Sort.Direction.DESC) Pageable pageable) {
		return eventoService.listar(status, papel, pageable);
	}

	@PostMapping
	@Operation(summary = "Criar evento em rascunho")
	public ResponseEntity<EventoDetalheResponse> criar(@Valid @RequestBody EventoRequest request) {
		EventoDetalheResponse resposta = eventoService.criar(request);
		return ResponseEntity.created(URI.create("/api/v1/eventos/" + resposta.id())).body(resposta);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Detalhar evento")
	public EventoDetalheResponse buscar(@PathVariable Long id) {
		return eventoService.buscar(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Editar dados do evento")
	public EventoDetalheResponse atualizar(@PathVariable Long id, @Valid @RequestBody EventoRequest request) {
		return eventoService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Excluir rascunho")
	public void excluir(@PathVariable Long id) {
		eventoService.excluir(id);
	}

	@PostMapping("/{id}/publicar")
	@Operation(summary = "Publicar rascunho")
	public EventoDetalheResponse publicar(@PathVariable Long id) {
		return eventoService.publicar(id);
	}

	@PostMapping("/{id}/encerrar")
	@Operation(summary = "Encerrar contribuições")
	public EventoDetalheResponse encerrar(@PathVariable Long id) {
		return eventoService.encerrar(id);
	}

	@PostMapping("/{id}/reabrir")
	@Operation(summary = "Reabrir evento encerrado")
	public EventoDetalheResponse reabrir(@PathVariable Long id, @Valid @RequestBody ReabrirRequest request) {
		return eventoService.reabrir(id, request);
	}

	@PostMapping("/{id}/concluir")
	@Operation(summary = "Marcar evento como realizado")
	public EventoDetalheResponse concluir(@PathVariable Long id) {
		return eventoService.concluir(id);
	}

	@PostMapping("/{id}/cancelar")
	@Operation(summary = "Cancelar evento")
	public EventoDetalheResponse cancelar(@PathVariable Long id) {
		return eventoService.cancelar(id);
	}

}
