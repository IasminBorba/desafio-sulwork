package br.com.sulwork.eventos.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		escreverProblema(response, HttpStatus.UNAUTHORIZED, "Não autenticado", "Faça login para continuar.");
	}

	static void escreverProblema(HttpServletResponse response, HttpStatus status, String titulo, String detalhe)
			throws IOException {
		response.setStatus(status.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getWriter().write("""
				{"type":"about:blank","title":"%s","status":%d,"detail":"%s"}
				""".formatted(titulo, status.value(), detalhe));
	}

}
