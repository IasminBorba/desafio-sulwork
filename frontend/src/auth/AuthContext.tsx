import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { atualizarPerfil as apiAtualizar, cadastrar as apiCadastrar, login as apiLogin, meuPerfil } from '../api/auth'
import { LOGOUT_EVENT, clearToken, getToken } from '../api/client'
import type { Usuario } from '../types/api'

interface AuthValue {
  usuario: Usuario | null
  carregando: boolean
  login: (email: string, senha: string) => Promise<void>
  cadastrar: (dados: {
    nome: string
    cpf: string
    email: string
    senha: string
    restricaoIds: number[]
  }) => Promise<void>
  logout: () => void
  atualizar: (dados: { nome: string; restricaoIds: number[] }) => Promise<void>
}

const AuthContext = createContext<AuthValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [carregando, setCarregando] = useState(true)

  const carregar = useCallback(async () => {
    if (!getToken()) {
      setUsuario(null)
      setCarregando(false)
      return
    }
    try {
      setUsuario(await meuPerfil())
    } catch {
      clearToken()
      setUsuario(null)
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    void carregar()
    const aoSair = () => setUsuario(null)
    window.addEventListener(LOGOUT_EVENT, aoSair)
    return () => window.removeEventListener(LOGOUT_EVENT, aoSair)
  }, [carregar])

  const login = useCallback(async (email: string, senha: string) => {
    await apiLogin(email, senha)
    setUsuario(await meuPerfil())
  }, [])

  const cadastrar = useCallback(async (dados: {
    nome: string
    cpf: string
    email: string
    senha: string
    restricaoIds: number[]
  }) => {
    await apiCadastrar(dados)
    await login(dados.email, dados.senha)
  }, [login])

  const logout = useCallback(() => {
    clearToken()
    setUsuario(null)
  }, [])

  const atualizar = useCallback(async (dados: { nome: string; restricaoIds: number[] }) => {
    setUsuario(await apiAtualizar(dados))
  }, [])

  const valor = useMemo(
    () => ({ usuario, carregando, login, cadastrar, logout, atualizar }),
    [usuario, carregando, login, cadastrar, logout, atualizar],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthValue {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth precisa estar dentro de AuthProvider')
  }
  return contexto
}
