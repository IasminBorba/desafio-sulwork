package br.com.sulwork.eventos.config;

import java.nio.charset.StandardCharsets;
import java.time.Clock;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

	public static final String EMISSOR = "sulwork-eventos";
	static final MacAlgorithm ALGORITMO = MacAlgorithm.HS256;

	@Bean
	SecretKey jwtSecretKey(JwtProperties properties) {
		byte[] segredo = properties.secret().getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(segredo, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSecretKey, Clock clock) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(ALGORITMO).build();
		JwtTimestampValidator validade = new JwtTimestampValidator();
		validade.setClock(clock);
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validade, new JwtIssuerValidator(EMISSOR)));
		return decoder;
	}

}
