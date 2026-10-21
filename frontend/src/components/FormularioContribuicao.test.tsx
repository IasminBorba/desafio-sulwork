import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import FormularioContribuicao from './FormularioContribuicao'
import type { EventoDetalhe } from '../types/api'

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    usuario: {
      id: 2,
      nome: 'Bia Lima',
      cpf: '24843803480',
      email: 'bia.lima@sulwork.demo',
      restricoes: [{ id: 1, nome: 'Doença celíaca' }],
    },
  }),
}))

vi.mock('../api/itens', () => ({
  listarItens: vi.fn(),
}))
vi.mock('../api/contribuicoes', () => ({
  listarContribuicoes: vi.fn(),
  criarContribuicao: vi.fn(),
  excluirContribuicao: vi.fn(),
}))
vi.mock('../api/cobertura', () => ({
  buscarCobertura: vi.fn(),
}))
vi.mock('../api/catalogos', () => ({
  listarRestricoes: vi.fn(),
}))

import { listarItens } from '../api/itens'
import { listarContribuicoes } from '../api/contribuicoes'
import { buscarCobertura } from '../api/cobertura'
import { listarRestricoes } from '../api/catalogos'

const evento: EventoDetalhe = {
  id: 9,
  nome: 'Churrasco',
  descricao: 'Aberto',
  tipo: 'CHURRASCO',
  status: 'ABERTO',
  dataInicio: '2026-10-25T15:00:00Z',
  dataFim: '2026-10-25T19:00:00Z',
  prazoAlteracao: '2026-10-24T15:00:00Z',
  organizador: { id: 1, nome: 'Ana' },
  aceitaAlteracoes: true,
  souOrganizador: false,
  estouParticipando: true,
}

describe('FormularioContribuicao', () => {
  beforeEach(() => {
    vi.mocked(listarItens).mockResolvedValue([
      {
        id: 4,
        nome: 'Pão de alho',
        categoria: { id: 1, nome: 'Acompanhamento' },
        quantidadeNecessaria: 5,
        obrigatorio: true,
        alergenos: [{ id: 10, nome: 'Glúten' }],
      },
    ])
    vi.mocked(listarContribuicoes).mockResolvedValue([])
    vi.mocked(buscarCobertura).mockResolvedValue({
      percentualGeral: 20,
      a: 0.2,
      b: null,
      c: 0.2,
      categorias: [],
      itens: [
        {
          itemId: 4,
          nome: 'Pão de alho',
          categoriaNome: 'Acompanhamento',
          obrigatorio: true,
          quantidadeNecessaria: 5,
          contribuido: 2,
          faltante: 3,
          percentual: 40,
        },
      ],
      obrigatoriosPendentes: [],
    })
    vi.mocked(listarRestricoes).mockResolvedValue([
      { id: 1, nome: 'Doença celíaca', alergenos: [{ id: 10, nome: 'Glúten' }] },
    ])
  })

  it('alerta restrição e limita pelo faltante', async () => {
    const usuario = userEvent.setup()
    render(<FormularioContribuicao evento={evento} />)
    const seletor = await screen.findByLabelText('Item')
    await usuario.selectOptions(seletor, '4')
    expect(await screen.findByRole('status')).toHaveTextContent('Glúten')
    expect(screen.getByText(/Restam 3/)).toBeInTheDocument()
    expect(screen.getByLabelText('Quantidade')).toHaveAttribute('max', '3')
  })
})
