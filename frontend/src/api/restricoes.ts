import type { RestricoesEvento } from '../types/api'
import { request } from './client'

export function buscarRestricoesEvento(eventoId: number) {
  return request<RestricoesEvento>(`/eventos/${eventoId}/restricoes`)
}
