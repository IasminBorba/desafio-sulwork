package br.com.sulwork.eventos.shared.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

	public static RecursoNaoEncontradoException de(String recurso, Object id) {
		return new RecursoNaoEncontradoException("%s com id %s não encontrado(a).".formatted(recurso, id));
	}

}
