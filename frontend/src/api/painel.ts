import type { Painel } from '../types/api'
import { request } from './client'

export function buscarPainel(eventoId: number) {
  return request<Painel>(`/eventos/${eventoId}/painel`)
}
