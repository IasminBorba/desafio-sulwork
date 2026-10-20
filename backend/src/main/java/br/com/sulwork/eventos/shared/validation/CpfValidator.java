package br.com.sulwork.eventos.shared.validation;

import br.com.sulwork.eventos.shared.domain.CpfUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CpfValido, String> {

	@Override
	public boolean isValid(String cpf, ConstraintValidatorContext context) {
		return cpf == null || CpfUtils.isValido(cpf);
	}

}
