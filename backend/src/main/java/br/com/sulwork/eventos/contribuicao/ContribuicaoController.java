package br.com.sulwork.eventos.contribuicao;

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

import br.com.sulwork.eventos.contribuicao.dto.ContribuicaoRequest;
import br.com.sulwork.eventos.contribuicao.dto.ContribuicaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/contribuicoes")
@Tag(name = "Contribuições")
public class ContribuicaoController {

	private final ContribuicaoService contribuicaoService;

	public ContribuicaoController(ContribuicaoService contribuicaoService) {
		this.contribuicaoService = contribuicaoService;
	}

	@GetMapping
	@Operation(summary = "Listar contribuições do evento")
	public List<ContribuicaoResponse> listar(@PathVariable Long eventoId) {
		return contribuicaoService.listar(eventoId);
	}

	@PostMapping
	@Operation(summary = "Registrar contribuição")
	public ResponseEntity<ContribuicaoResponse> criar(@PathVariable Long eventoId,
			@Valid @RequestBody ContribuicaoRequest request) {
		ContribuicaoResponse resposta = contribuicaoService.criar(eventoId, request);
		return ResponseEntity.created(URI.create("/api/v1/eventos/" + eventoId + "/contribuicoes/" + resposta.id()))
				.body(resposta);
	}

	@PutMapping("/{contribuicaoId}")
	@Operation(summary = "Alterar a própria contribuição")
	public ContribuicaoResponse atualizar(@PathVariable Long eventoId, @PathVariable Long contribuicaoId,
			@Valid @RequestBody ContribuicaoRequest request) {
		return contribuicaoService.atualizar(eventoId, contribuicaoId, request);
	}

	@DeleteMapping("/{contribuicaoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remover a própria contribuição")
	public void excluir(@PathVariable Long eventoId, @PathVariable Long contribuicaoId) {
		contribuicaoService.excluir(eventoId, contribuicaoId);
	}

}
