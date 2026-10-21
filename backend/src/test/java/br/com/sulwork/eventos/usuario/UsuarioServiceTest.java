package br.com.sulwork.eventos.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sulwork.eventos.catalogo.RestricaoAlimentar;
import br.com.sulwork.eventos.catalogo.RestricaoCatalogo;
import br.com.sulwork.eventos.usuario.dto.AtualizarPerfilRequest;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioAutenticado usuarioAutenticado;

	@Mock
	private RestricaoCatalogo restricaoCatalogo;

	@InjectMocks
	private UsuarioService usuarioService;

	@Test
	void atualizaNomeERestricoes() {
		Usuario usuario = Usuario.novo("Ana", "11144477735", "ana@example.com", "hash");
		usuario.setId(1L);
		RestricaoAlimentar vegano = org.mockito.Mockito.mock(RestricaoAlimentar.class);
		when(vegano.getId()).thenReturn(12L);
		when(vegano.getNome()).thenReturn("Vegano");
		when(usuarioAutenticado.comRestricoes()).thenReturn(usuario);
		when(restricaoCatalogo.buscar(List.of(12L))).thenReturn(Set.of(vegano));

		var resposta = usuarioService.atualizarPerfil(new AtualizarPerfilRequest(" Ana Clara ", List.of(12L)));

		assertThat(resposta.nome()).isEqualTo("Ana Clara");
		assertThat(resposta.restricoes()).extracting("nome").containsExactly("Vegano");
		assertThat(usuario.getNome()).isEqualTo("Ana Clara");
	}

}
