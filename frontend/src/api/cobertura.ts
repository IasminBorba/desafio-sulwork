import type { Cobertura } from '../types/api'
import { request } from './client'

export function buscarCobertura(eventoId: number) {
  return request<Cobertura>(`/eventos/${eventoId}/cobertura`)
}
