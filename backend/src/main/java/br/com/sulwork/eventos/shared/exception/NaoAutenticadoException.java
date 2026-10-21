package br.com.sulwork.eventos.shared.exception;

public class NaoAutenticadoException extends RuntimeException {

	public NaoAutenticadoException(String mensagem) {
		super(mensagem);
	}

}
