package br.com.sulwork.eventos.auth;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.sulwork.eventos.config.JwtConfig;
import br.com.sulwork.eventos.config.JwtProperties;
import br.com.sulwork.eventos.usuario.Usuario;

@Service
public class TokenService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties properties;
	private final Clock clock;

	public TokenService(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
		this.clock = clock;
	}

	public TokenGerado gerar(Usuario usuario) {
		Instant agora = clock.instant();
		Instant expiraEm = agora.plus(properties.expiration());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(JwtConfig.EMISSOR)
				.issuedAt(agora)
				.expiresAt(expiraEm)
				.subject(usuario.getId().toString())
				.claim("email", usuario.getEmail())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String valor = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new TokenGerado(valor, expiraEm);
	}

}
