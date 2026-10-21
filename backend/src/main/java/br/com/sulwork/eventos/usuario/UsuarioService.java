package br.com.sulwork.eventos.usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.catalogo.RestricaoCatalogo;
import br.com.sulwork.eventos.usuario.dto.AtualizarPerfilRequest;
import br.com.sulwork.eventos.usuario.dto.UsuarioResponse;

@Service
public class UsuarioService {

	private final UsuarioAutenticado usuarioAutenticado;
	private final RestricaoCatalogo restricaoCatalogo;

	public UsuarioService(UsuarioAutenticado usuarioAutenticado, RestricaoCatalogo restricaoCatalogo) {
		this.usuarioAutenticado = usuarioAutenticado;
		this.restricaoCatalogo = restricaoCatalogo;
	}

	@Transactional(readOnly = true)
	public UsuarioResponse meuPerfil() {
		return UsuarioResponse.de(usuarioAutenticado.comRestricoes());
	}

	@Transactional
	public UsuarioResponse atualizarPerfil(AtualizarPerfilRequest request) {
		Usuario usuario = usuarioAutenticado.comRestricoes();
		usuario.atualizarPerfil(request.nome().strip(), restricaoCatalogo.buscar(request.restricaoIds()));
		return UsuarioResponse.de(usuario);
	}

}
