import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ProtectedRoute from './ProtectedRoute'
import { useAuth } from './AuthContext'

vi.mock('./AuthContext', () => ({
  useAuth: vi.fn(),
}))

const useAuthMock = vi.mocked(useAuth)

function renderComRota() {
  return render(
    <MemoryRouter initialEntries={['/privado']}>
      <Routes>
        <Route path="/login" element={<p>Tela de login</p>} />
        <Route element={<ProtectedRoute />}>
          <Route path="/privado" element={<p>Área logada</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    useAuthMock.mockReset()
  })

  it('redireciona para o login sem usuário', () => {
    useAuthMock.mockReturnValue({
      usuario: null,
      carregando: false,
      login: vi.fn(),
      cadastrar: vi.fn(),
      logout: vi.fn(),
      atualizar: vi.fn(),
    })
    renderComRota()
    expect(screen.getByText('Tela de login')).toBeInTheDocument()
  })

  it('mostra a área protegida quando há sessão', () => {
    useAuthMock.mockReturnValue({
      usuario: {
        id: 1,
        nome: 'Ana Souza',
        cpf: '71428793860',
        email: 'ana.souza@sulwork.demo',
        restricoes: [],
      },
      carregando: false,
      login: vi.fn(),
      cadastrar: vi.fn(),
      logout: vi.fn(),
      atualizar: vi.fn(),
    })
    renderComRota()
    expect(screen.getByText('Área logada')).toBeInTheDocument()
  })
})
