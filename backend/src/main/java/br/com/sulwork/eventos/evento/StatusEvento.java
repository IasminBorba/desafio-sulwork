package br.com.sulwork.eventos.evento;

import java.util.Set;

public enum StatusEvento {

	RASCUNHO,
	ABERTO,
	ENCERRADO,
	REALIZADO,
	CANCELADO;

	public boolean podeIrPara(StatusEvento destino) {
		return destinosPermitidos().contains(destino);
	}

	private Set<StatusEvento> destinosPermitidos() {
		return switch (this) {
			case RASCUNHO -> Set.of(ABERTO, CANCELADO);
			case ABERTO -> Set.of(ENCERRADO, CANCELADO);
			case ENCERRADO -> Set.of(ABERTO, REALIZADO, CANCELADO);
			case REALIZADO, CANCELADO -> Set.of();
		};
	}

}
