package br.com.sulwork.eventos.shared.domain;

import java.util.regex.Pattern;

/**
 * Normalização e validação de CPF pelos dígitos verificadores (Módulo 11).
 */
public final class CpfUtils {

	private static final Pattern FORMATO_ACEITO = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");
	private static final int TAMANHO = 11;

	private CpfUtils() {
	}

	/**
	 * Remove a máscara ({@code 111.444.777-35} vira {@code 11144477735}).
	 * Retorna {@code null} se o texto não estiver em um formato de CPF aceito.
	 */
	public static String normalizar(String cpf) {
		if (cpf == null) {
			return null;
		}
		String semEspacos = cpf.strip();
		if (!FORMATO_ACEITO.matcher(semEspacos).matches()) {
			return null;
		}
		return semEspacos.replaceAll("\\D", "");
	}

	public static boolean isValido(String cpf) {
		String digitos = normalizar(cpf);
		if (digitos == null || todosDigitosIguais(digitos)) {
			return false;
		}
		int[] numeros = digitos.chars().map(c -> c - '0').toArray();
		return numeros[9] == calcularDigitoVerificador(numeros, 9)
				&& numeros[10] == calcularDigitoVerificador(numeros, 10);
	}

	/**
	 * Calcula o dígito verificador a partir dos {@code quantidade} primeiros dígitos,
	 * com pesos decrescentes de {@code quantidade + 1} até 2.
	 */
	private static int calcularDigitoVerificador(int[] numeros, int quantidade) {
		int soma = 0;
		int peso = quantidade + 1;
		for (int i = 0; i < quantidade; i++) {
			soma += numeros[i] * peso--;
		}
		int resto = soma % TAMANHO;
		return resto < 2 ? 0 : TAMANHO - resto;
	}

	// 111.111.111-11 e similares passam no Módulo 11, mas a Receita Federal não os emite.
	private static boolean todosDigitosIguais(String digitos) {
		return digitos.chars().allMatch(c -> c == digitos.charAt(0));
	}

}
