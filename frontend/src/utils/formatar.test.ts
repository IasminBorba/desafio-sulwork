import { describe, expect, it } from 'vitest'
import { formatarData } from './formatar'

describe('formatarData', () => {
  it('formata no fuso America/Recife', () => {
    const texto = formatarData('2026-10-21T15:00:00Z')
    expect(texto).toMatch(/21\/10\/2026/)
    expect(texto).toMatch(/12:00/)
  })
})
