package br.com.sulwork.eventos.restricao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.restricao.dto.RestricoesEventoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/restricoes")
@Tag(name = "Restrições do evento")
public class RestricaoEventoController {

	private final RestricaoEventoService restricaoEventoService;

	public RestricaoEventoController(RestricaoEventoService restricaoEventoService) {
		this.restricaoEventoService = restricaoEventoService;
	}

	@GetMapping
	@Operation(summary = "Cruzar restrições dos participantes com os itens")
	public RestricoesEventoResponse analisar(@PathVariable Long eventoId) {
		return restricaoEventoService.analisar(eventoId);
	}

}
