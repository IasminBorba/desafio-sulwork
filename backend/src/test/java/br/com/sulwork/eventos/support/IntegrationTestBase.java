package br.com.sulwork.eventos.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import br.com.sulwork.eventos.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RelogioTesteConfig.class })
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

	@Autowired
	protected RelogioAjustavel relogio;

	@BeforeEach
	void resetarRelogioDeTeste() {
		relogio.reset();
	}

}
