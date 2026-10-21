package br.com.sulwork.eventos.auth;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sulwork.eventos.auth.dto.CadastroRequest;
import br.com.sulwork.eventos.auth.dto.LoginRequest;
import br.com.sulwork.eventos.auth.dto.LoginResponse;
import br.com.sulwork.eventos.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação")
@SecurityRequirements
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/cadastro")
	@Operation(summary = "Cadastrar colaborador")
	public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
		UsuarioResponse resposta = authService.cadastrar(request);
		return ResponseEntity.created(URI.create("/api/v1/usuarios/me")).body(resposta);
	}

	@PostMapping("/login")
	@Operation(summary = "Autenticar e obter token JWT")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

}
