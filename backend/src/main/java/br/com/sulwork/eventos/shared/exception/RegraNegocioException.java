package br.com.sulwork.eventos.shared.exception;

/**
 * Requisição bem formada, mas que viola uma regra de negócio.
 */
public class RegraNegocioException extends RuntimeException {

	public RegraNegocioException(String mensagem) {
		super(mensagem);
	}

}
