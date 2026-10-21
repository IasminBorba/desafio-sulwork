import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import type { EventoDetalhe } from '../types/api'
import AcoesStatus from './AcoesStatus'

const base: EventoDetalhe = {
  id: 1,
  nome: 'Churrasco',
  descricao: 'Equipe',
  tipo: 'CHURRASCO',
  status: 'RASCUNHO',
  dataInicio: '2026-10-25T15:00:00Z',
  dataFim: '2026-10-25T19:00:00Z',
  prazoAlteracao: '2026-10-24T15:00:00Z',
  organizador: { id: 1, nome: 'Ana' },
  aceitaAlteracoes: true,
  souOrganizador: true,
  estouParticipando: false,
}

describe('AcoesStatus', () => {
  it('não mostra botões para quem não organiza', () => {
    render(<AcoesStatus evento={{ ...base, souOrganizador: false }} onAcao={vi.fn()} enviando={false} />)
    expect(screen.queryByTestId('acoes-status')).not.toBeInTheDocument()
  })

  it('mostra publicar só no rascunho', () => {
    render(<AcoesStatus evento={base} onAcao={vi.fn()} enviando={false} />)
    expect(screen.getByText('Publicar')).toBeInTheDocument()
    expect(screen.queryByText('Encerrar contribuições')).not.toBeInTheDocument()
  })

  it('mostra encerrar só quando aberto', () => {
    render(<AcoesStatus evento={{ ...base, status: 'ABERTO' }} onAcao={vi.fn()} enviando={false} />)
    expect(screen.getByText('Encerrar contribuições')).toBeInTheDocument()
    expect(screen.queryByText('Publicar')).not.toBeInTheDocument()
  })

  it('mostra reabrir quando encerrado', () => {
    render(<AcoesStatus evento={{ ...base, status: 'ENCERRADO' }} onAcao={vi.fn()} enviando={false} />)
    expect(screen.getByRole('button', { name: 'Reabrir' })).toBeInTheDocument()
    expect(screen.getByText('Marcar como realizado')).toBeInTheDocument()
  })
})
