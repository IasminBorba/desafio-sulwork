package br.com.sulwork.eventos.auth;

import java.time.Instant;

public record TokenGerado(String valor, Instant expiraEm) {
}
