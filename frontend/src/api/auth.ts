import type { LoginResponse, Usuario } from '../types/api'
import { request, setToken } from './client'

export async function cadastrar(body: {
  nome: string
  cpf: string
  email: string
  senha: string
  restricaoIds: number[]
}): Promise<Usuario> {
  return request<Usuario>('/auth/cadastro', { method: 'POST', body, auth: false })
}

export async function login(email: string, senha: string): Promise<LoginResponse> {
  const resposta = await request<LoginResponse>('/auth/login', {
    method: 'POST',
    body: { email, senha },
    auth: false,
  })
  setToken(resposta.token)
  return resposta
}

export async function meuPerfil(): Promise<Usuario> {
  return request<Usuario>('/usuarios/me')
}

export async function atualizarPerfil(body: { nome: string; restricaoIds: number[] }): Promise<Usuario> {
  return request<Usuario>('/usuarios/me', { method: 'PUT', body })
}
