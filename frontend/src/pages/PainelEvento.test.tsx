import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import PainelEvento from './PainelEvento'

vi.mock('../api/painel', () => ({
  buscarPainel: vi.fn(),
}))

import { buscarPainel } from '../api/painel'

describe('PainelEvento', () => {
  it('destaca quem não contribuiu e alterna a visão', async () => {
    vi.mocked(buscarPainel).mockResolvedValue({
      evento: {
        id: 9,
        nome: 'Churrasco da equipe',
        descricao: 'Aberto',
        tipo: 'CHURRASCO',
        status: 'ABERTO',
        dataInicio: '2026-10-25T15:00:00Z',
        dataFim: '2026-10-25T19:00:00Z',
        prazoAlteracao: '2026-10-24T15:00:00Z',
        organizador: { id: 1, nome: 'Ana' },
        aceitaAlteracoes: true,
        souOrganizador: true,
        estouParticipando: true,
      },
      cobertura: {
        percentualGeral: 61,
        a: 0.7,
        b: 0.5,
        c: 0.5,
        categorias: [],
        itens: [],
        obrigatoriosPendentes: [],
      },
      restricoes: { resumo: [], porItem: [], semItemCompativel: [] },
      participantes: [
        {
          participacaoId: 1,
          usuarioId: 1,
          nome: 'Ana Souza',
          cpf: '714.***.***-60',
          semContribuicao: false,
          restricoes: [],
          contribuicoes: [{ itemId: 4, itemNome: 'Carvão', categoriaNome: 'Outros', quantidade: 2 }],
        },
        {
          participacaoId: 2,
          usuarioId: 2,
          nome: 'Bia Lima',
          cpf: '248.***.***-80',
          semContribuicao: true,
          restricoes: [{ id: 1, nome: 'Doença celíaca' }],
          contribuicoes: [],
        },
      ],
    })

    const usuario = userEvent.setup()
    render(
      <MemoryRouter initialEntries={['/eventos/9/painel']}>
        <Routes>
          <Route path="/eventos/:id/painel" element={<PainelEvento />} />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByText('Churrasco da equipe')).toBeInTheDocument()
    expect(screen.getByText(/Ainda não contribuíram/)).toBeInTheDocument()
    const blocoBia = screen.getByRole('heading', { name: 'Bia Lima' }).closest('article')
    expect(blocoBia).toHaveClass('destaque')

    await usuario.click(screen.getByRole('button', { name: 'Por item' }))
    expect(screen.getByRole('heading', { name: 'Carvão' })).toBeInTheDocument()
    expect(screen.getByText(/Ana Souza: 2/)).toBeInTheDocument()
  })
})
