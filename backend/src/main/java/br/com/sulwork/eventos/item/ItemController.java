package br.com.sulwork.eventos.item;

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

import br.com.sulwork.eventos.item.dto.ItemRequest;
import br.com.sulwork.eventos.item.dto.ItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/itens")
@Tag(name = "Itens")
public class ItemController {

	private final ItemService itemService;

	public ItemController(ItemService itemService) {
		this.itemService = itemService;
	}

	@GetMapping
	@Operation(summary = "Listar itens do evento")
	public List<ItemResponse> listar(@PathVariable Long eventoId) {
		return itemService.listar(eventoId);
	}

	@PostMapping
	@Operation(summary = "Adicionar item ao evento")
	public ResponseEntity<ItemResponse> criar(@PathVariable Long eventoId, @Valid @RequestBody ItemRequest request) {
		ItemResponse resposta = itemService.criar(eventoId, request);
		return ResponseEntity.created(URI.create("/api/v1/eventos/" + eventoId + "/itens/" + resposta.id()))
				.body(resposta);
	}

	@PutMapping("/{itemId}")
	@Operation(summary = "Editar item do evento")
	public ItemResponse atualizar(@PathVariable Long eventoId, @PathVariable Long itemId,
			@Valid @RequestBody ItemRequest request) {
		return itemService.atualizar(eventoId, itemId, request);
	}

	@DeleteMapping("/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remover item do evento")
	public void excluir(@PathVariable Long eventoId, @PathVariable Long itemId) {
		itemService.excluir(eventoId, itemId);
	}

}
