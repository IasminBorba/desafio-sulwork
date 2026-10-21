package br.com.sulwork.eventos.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class RelogioTesteConfig {

	@Bean
	@Primary
	RelogioAjustavel relogioAjustavel() {
		return new RelogioAjustavel();
	}

}
