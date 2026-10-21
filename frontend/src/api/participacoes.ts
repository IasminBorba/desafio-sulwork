import type { Participacao } from '../types/api'
import { request } from './client'

export function listarParticipacoes(eventoId: number) {
  return request<Participacao[]>(`/eventos/${eventoId}/participacoes`)
}

export function participar(eventoId: number) {
  return request<Participacao>(`/eventos/${eventoId}/participacoes`, { method: 'POST' })
}

export function cancelarParticipacao(eventoId: number, participacaoId: number) {
  return request<void>(`/eventos/${eventoId}/participacoes/${participacaoId}`, { method: 'DELETE' })
}
