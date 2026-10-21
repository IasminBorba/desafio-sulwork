package br.com.sulwork.eventos.painel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.painel.dto.PainelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/painel")
@Tag(name = "Painel")
public class PainelController {

	private final PainelService painelService;

	public PainelController(PainelService painelService) {
		this.painelService = painelService;
	}

	@GetMapping
	@Operation(summary = "Painel consolidado do organizador")
	public PainelResponse montar(@PathVariable Long eventoId) {
		return painelService.montar(eventoId);
	}

}
