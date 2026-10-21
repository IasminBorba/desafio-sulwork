package br.com.sulwork.eventos.cobertura;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.cobertura.dto.CoberturaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/cobertura")
@Tag(name = "Cobertura")
public class CoberturaController {

	private final CoberturaService coberturaService;

	public CoberturaController(CoberturaService coberturaService) {
		this.coberturaService = coberturaService;
	}

	@GetMapping
	@Operation(summary = "Calcular cobertura do evento")
	public CoberturaResponse calcular(@PathVariable Long eventoId) {
		return coberturaService.calcular(eventoId);
	}

}
