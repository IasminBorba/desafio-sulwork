package br.com.sulwork.eventos.usuario;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.usuario.dto.AtualizarPerfilRequest;
import br.com.sulwork.eventos.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuário")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/me")
	@Operation(summary = "Consultar o próprio perfil")
	public UsuarioResponse me() {
		return usuarioService.meuPerfil();
	}

	@PutMapping("/me")
	@Operation(summary = "Atualizar nome e restrições alimentares")
	public UsuarioResponse atualizar(@Valid @RequestBody AtualizarPerfilRequest request) {
		return usuarioService.atualizarPerfil(request);
	}

}
