package br.com.sulwork.eventos.usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import br.com.sulwork.eventos.shared.exception.NaoAutenticadoException;
import br.com.sulwork.eventos.shared.exception.RecursoNaoEncontradoException;

@Component
public class UsuarioAutenticado {

	private final UsuarioRepository usuarioRepository;

	public UsuarioAutenticado(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	public Long id() {
		return Long.valueOf(jwt().getSubject());
	}

	public Usuario get() {
		return usuarioRepository.findById(id())
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", id()));
	}

	public Usuario comRestricoes() {
		return usuarioRepository.findComRestricoesById(id())
				.orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", id()));
	}

	private Jwt jwt() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt jwt)) {
			throw new NaoAutenticadoException("Faça login para continuar.");
		}
		return jwt;
	}

}
