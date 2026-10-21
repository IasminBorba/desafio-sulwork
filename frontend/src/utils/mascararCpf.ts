export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '').slice(0, 11)
}

export function formatarCpfInput(valor: string): string {
  const digitos = somenteDigitos(valor)
  return digitos
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
}

export function mascararCpf(cpf: string): string {
  const digitos = somenteDigitos(cpf)
  if (digitos.length !== 11) {
    return cpf
  }
  return `${digitos.slice(0, 3)}.***.***-${digitos.slice(9)}`
}
