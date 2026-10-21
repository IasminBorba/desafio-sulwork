package br.com.sulwork.eventos.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.sulwork.eventos.auth.dto.CadastroRequest;
import br.com.sulwork.eventos.auth.dto.LoginRequest;
import br.com.sulwork.eventos.catalogo.RestricaoCatalogo;
import br.com.sulwork.eventos.shared.exception.ConflitoException;
import br.com.sulwork.eventos.shared.exception.NaoAutenticadoException;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private RestricaoCatalogo restricaoCatalogo;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private TokenService tokenService;

	@InjectMocks
	private AuthService authService;

	@Test
	void cadastraNormalizandoCpfEmailECriptografandoSenha() {
		when(usuarioRepository.existsByCpf("11144477735")).thenReturn(false);
		when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
		when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
		when(restricaoCatalogo.buscar(List.of(12L))).thenReturn(Set.of());
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
			Usuario salvo = invocacao.getArgument(0);
			salvo.setId(1L);
			return salvo;
		});

		var resposta = authService.cadastrar(new CadastroRequest(
				" Ana ", "111.444.777-35", "Ana@Example.com", "senha123", List.of(12L)));

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(captor.capture());
		Usuario persistido = captor.getValue();
		assertThat(persistido.getCpf()).isEqualTo("11144477735");
		assertThat(persistido.getEmail()).isEqualTo("ana@example.com");
		assertThat(persistido.getSenhaHash()).isEqualTo("hash-bcrypt");
		assertThat(resposta.id()).isEqualTo(1L);
		assertThat(resposta.email()).isEqualTo("ana@example.com");
	}

	@Test
	void cadastroComCpfDuplicadoRetorna409() {
		when(usuarioRepository.existsByCpf("11144477735")).thenReturn(true);

		assertThatThrownBy(() -> authService.cadastrar(new CadastroRequest(
				"Ana", "11144477735", "ana@example.com", "senha123", null)))
				.isInstanceOf(ConflitoException.class)
				.hasMessage("Já existe um usuário com este CPF.");
	}

	@Test
	void cadastroComEmailDuplicadoRetorna409() {
		when(usuarioRepository.existsByCpf("11144477735")).thenReturn(false);
		when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.cadastrar(new CadastroRequest(
				"Ana", "11144477735", "ANA@example.com", "senha123", null)))
				.isInstanceOf(ConflitoException.class)
				.hasMessage("Já existe um usuário com este e-mail.");
	}

	@Test
	void loginDevolveToken() {
		Usuario usuario = Usuario.novo("Ana", "11144477735", "ana@example.com", "hash");
		usuario.setId(7L);
		when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
		when(tokenService.gerar(usuario)).thenReturn(new TokenGerado("jwt", Instant.parse("2026-06-15T23:00:00Z")));

		var resposta = authService.login(new LoginRequest("Ana@Example.com", "senha123"));

		assertThat(resposta.token()).isEqualTo("jwt");
		assertThat(resposta.expiraEm()).isEqualTo(Instant.parse("2026-06-15T23:00:00Z"));
	}

	@Test
	void loginComSenhaErradaRetorna401() {
		Usuario usuario = Usuario.novo("Ana", "11144477735", "ana@example.com", "hash");
		when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

		assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "errada")))
				.isInstanceOf(NaoAutenticadoException.class)
				.hasMessage("E-mail ou senha inválidos.");
	}

	@Test
	void loginComEmailInexistenteRetorna401() {
		when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "senha123")))
				.isInstanceOf(NaoAutenticadoException.class)
				.hasMessage("E-mail ou senha inválidos.");
	}

}
