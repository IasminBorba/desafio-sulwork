import type { Contribuicao } from '../types/api'
import { request } from './client'

export function listarContribuicoes(eventoId: number) {
  return request<Contribuicao[]>(`/eventos/${eventoId}/contribuicoes`)
}

export function criarContribuicao(eventoId: number, body: { itemId: number; quantidade: number }) {
  return request<Contribuicao>(`/eventos/${eventoId}/contribuicoes`, { method: 'POST', body })
}

export function atualizarContribuicao(
  eventoId: number,
  contribuicaoId: number,
  body: { itemId: number; quantidade: number },
) {
  return request<Contribuicao>(`/eventos/${eventoId}/contribuicoes/${contribuicaoId}`, { method: 'PUT', body })
}

export function excluirContribuicao(eventoId: number, contribuicaoId: number) {
  return request<void>(`/eventos/${eventoId}/contribuicoes/${contribuicaoId}`, { method: 'DELETE' })
}
