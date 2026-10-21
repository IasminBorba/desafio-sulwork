package br.com.sulwork.eventos.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.auth.dto.CadastroRequest;
import br.com.sulwork.eventos.auth.dto.LoginRequest;
import br.com.sulwork.eventos.auth.dto.LoginResponse;
import br.com.sulwork.eventos.catalogo.RestricaoCatalogo;
import br.com.sulwork.eventos.shared.domain.CpfUtils;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.NaoAutenticadoException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioRepository;
import br.com.sulwork.eventos.usuario.dto.UsuarioResponse;

@Service
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final RestricaoCatalogo restricaoCatalogo;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public AuthService(UsuarioRepository usuarioRepository, RestricaoCatalogo restricaoCatalogo,
			PasswordEncoder passwordEncoder, TokenService tokenService) {
		this.usuarioRepository = usuarioRepository;
		this.restricaoCatalogo = restricaoCatalogo;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	@Transactional
	public UsuarioResponse cadastrar(CadastroRequest request) {
		String cpf = CpfUtils.normalizar(request.cpf());
		String email = request.email().strip().toLowerCase();
		if (usuarioRepository.existsByCpf(cpf)) {
			throw new ConflitoException("Já existe um usuário com este CPF.");
		}
		if (usuarioRepository.existsByEmail(email)) {
			throw new ConflitoException("Já existe um usuário com este e-mail.");
		}
		Usuario usuario = Usuario.novo(
				request.nome().strip(),
				cpf,
				email,
				passwordEncoder.encode(request.senha()));
		usuario.getRestricoes().addAll(restricaoCatalogo.buscar(request.restricaoIds()));
		return UsuarioResponse.de(usuarioRepository.save(usuario));
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		String email = request.email().strip().toLowerCase();
		Usuario usuario = usuarioRepository.findByEmail(email)
				.filter(encontrado -> passwordEncoder.matches(request.senha(), encontrado.getSenhaHash()))
				.orElseThrow(() -> new NaoAutenticadoException("E-mail ou senha inválidos."));
		TokenGerado token = tokenService.gerar(usuario);
		return new LoginResponse(token.valor(), token.expiraEm());
	}

}
