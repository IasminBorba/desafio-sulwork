package br.com.sulwork.eventos.catalogo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.com.sulwork.eventos.TestcontainersConfiguration;
import br.com.sulwork.eventos.evento.Evento;
import br.com.sulwork.eventos.evento.EventoRepository;
import br.com.sulwork.eventos.evento.TipoEvento;
import br.com.sulwork.eventos.usuario.Usuario;
import br.com.sulwork.eventos.usuario.UsuarioRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class MapeamentoJpaTest {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private AlergenoRepository alergenoRepository;

	@Autowired
	private RestricaoAlimentarRepository restricaoAlimentarRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private EventoRepository eventoRepository;

	@Test
	void seedDosCatalogosFoiCarregado() {
		assertThat(categoriaRepository.count()).isEqualTo(10);
		assertThat(alergenoRepository.count()).isEqualTo(11);
		assertThat(restricaoAlimentarRepository.count()).isEqualTo(12);
	}

	@Test
	void veganoEvitaOsComponentesDeOrigemAnimal() {
		RestricaoAlimentar vegano = restricaoAlimentarRepository.findByNome("Vegano").orElseThrow();

		assertThat(vegano.getAlergenos())
				.extracting(Alergeno::getNome)
				.containsExactlyInAnyOrder("Carne", "Peixe", "Crustáceos", "Leite e derivados", "Ovo", "Mel");
	}

	@Test
	@Transactional
	void auditoriaPreencheDatasAoPersistir() {
		Usuario organizador = usuarioRepository.saveAndFlush(
				Usuario.novo("Ana", "11144477735", "ana@example.com", "hash"));
		Instant agora = Instant.now();
		Evento evento = eventoRepository.saveAndFlush(Evento.novo(
				"Churrasco da equipe",
				"Celebração",
				TipoEvento.CHURRASCO,
				agora.plus(2, ChronoUnit.DAYS),
				agora.plus(2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
				agora.plus(1, ChronoUnit.DAYS),
				organizador));

		assertThat(organizador.getId()).isNotNull();
		assertThat(organizador.getCriadoEm()).isNotNull();
		assertThat(organizador.getAtualizadoEm()).isNotNull();
		assertThat(evento.getStatus().name()).isEqualTo("RASCUNHO");
		assertThat(evento.getCriadoEm()).isNotNull();
	}

}
