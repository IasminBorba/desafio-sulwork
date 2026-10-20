package br.com.sulwork.eventos.shared.exception;

/**
 * Usuário autenticado, mas sem permissão sobre o recurso (por exemplo, não é o organizador do evento).
 */
public class PermissaoNegadaException extends RuntimeException {

	public PermissaoNegadaException(String mensagem) {
		super(mensagem);
	}

}
