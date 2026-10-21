import { describe, expect, it } from 'vitest'
import { formatarCpfInput, mascararCpf, somenteDigitos } from './mascararCpf'

describe('mascararCpf', () => {
  it('mascara os dígitos do meio', () => {
    expect(mascararCpf('11144477735')).toBe('111.***.***-35')
  })

  it('aceita CPF já formatado', () => {
    expect(mascararCpf('111.444.777-35')).toBe('111.***.***-35')
  })
})

describe('formatarCpfInput', () => {
  it('aplica a máscara progressiva', () => {
    expect(somenteDigitos('111.444.777-35')).toBe('11144477735')
    expect(formatarCpfInput('11144477735')).toBe('111.444.777-35')
  })
})
