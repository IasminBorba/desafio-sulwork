package br.com.sulwork.eventos.participacao;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.participacao.dto.ParticipacaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/participacoes")
@Tag(name = "Participações")
public class ParticipacaoController {

	private final ParticipacaoService participacaoService;

	public ParticipacaoController(ParticipacaoService participacaoService) {
		this.participacaoService = participacaoService;
	}

	@GetMapping
	@Operation(summary = "Listar participantes do evento")
	public List<ParticipacaoResponse> listar(@PathVariable Long eventoId) {
		return participacaoService.listar(eventoId);
	}

	@PostMapping
	@Operation(summary = "Registrar a própria participação")
	public ResponseEntity<ParticipacaoResponse> participar(@PathVariable Long eventoId) {
		ParticipacaoResponse resposta = participacaoService.participar(eventoId);
		return ResponseEntity.created(URI.create("/api/v1/eventos/" + eventoId + "/participacoes/" + resposta.id()))
				.body(resposta);
	}

	@DeleteMapping("/{participacaoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Cancelar participação")
	public void cancelar(@PathVariable Long eventoId, @PathVariable Long participacaoId) {
		participacaoService.cancelar(eventoId, participacaoId);
	}

}
