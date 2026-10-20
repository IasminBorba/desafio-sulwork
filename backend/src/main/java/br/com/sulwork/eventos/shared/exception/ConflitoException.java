package br.com.sulwork.eventos.shared.exception;

/**
 * Operação conflita com o estado atual do recurso (status, prazo, duplicidade, limite de quantidade).
 */
public class ConflitoException extends RuntimeException {

	public ConflitoException(String mensagem) {
		super(mensagem);
	}

}
