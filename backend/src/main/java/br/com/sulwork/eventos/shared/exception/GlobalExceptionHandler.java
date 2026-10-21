package br.com.sulwork.eventos.shared.exception;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.PropertyResolutionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	ProblemDetail handleNaoEncontrado(RecursoNaoEncontradoException ex) {
		return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
	}

	@ExceptionHandler(NaoAutenticadoException.class)
	ProblemDetail handleNaoAutenticado(NaoAutenticadoException ex) {
		return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", ex.getMessage());
	}

	@ExceptionHandler(PermissaoNegadaException.class)
	ProblemDetail handlePermissaoNegada(PermissaoNegadaException ex) {
		return problema(HttpStatus.FORBIDDEN, "Permissão negada", ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleIntegridade(DataIntegrityViolationException ex) {
		log.warn("Violação de integridade", ex);
		return problema(HttpStatus.CONFLICT, "Conflito", "Os dados informados conflitam com um registro existente.");
	}

	@ExceptionHandler(ConflitoException.class)
	ProblemDetail handleConflito(ConflitoException ex) {
		return problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
	}

	@ExceptionHandler(RegraNegocioException.class)
	ProblemDetail handleRegraNegocio(RegraNegocioException ex) {
		return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", ex.getMessage());
	}

	@ExceptionHandler({ PropertyReferenceException.class, PropertyResolutionException.class })
	ProblemDetail handleOrdenacaoInvalida(RuntimeException ex) {
		return problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
				"Parâmetro de ordenação inválido. Use um campo do evento, por exemplo dataInicio,desc.");
	}

	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	ProblemDetail handleAcessoInvalido(InvalidDataAccessApiUsageException ex) {
		if (eOrdenacaoInvalida(ex)) {
			return handleOrdenacaoInvalida(ex);
		}
		log.error("Erro inesperado", ex);
		return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado.");
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleInesperado(Exception ex) {
		log.error("Erro inesperado", ex);
		return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado.");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
				"Um ou mais campos estão inválidos.");
		List<Map<String, String>> erros = ex.getBindingResult().getFieldErrors().stream()
				.map(GlobalExceptionHandler::erroDeCampo)
				.toList();
		problema.setProperty("erros", erros);
		return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
	}

	private static Map<String, String> erroDeCampo(FieldError erro) {
		String mensagem = erro.getDefaultMessage() == null ? "inválido" : erro.getDefaultMessage();
		return Map.of("campo", erro.getField(), "mensagem", mensagem);
	}

	private static boolean eOrdenacaoInvalida(Throwable ex) {
		while (ex != null) {
			String mensagem = ex.getMessage();
			if (mensagem != null && mensagem.toLowerCase(Locale.ROOT).contains("sort")) {
				return true;
			}
			ex = ex.getCause();
		}
		return false;
	}

	private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
		problema.setTitle(titulo);
		return problema;
	}

}
