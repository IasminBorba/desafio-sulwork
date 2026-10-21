import type { EventoDetalhe, EventoResumo, Pagina, PapelEvento, StatusEvento, TipoEvento } from '../types/api'
import { request } from './client'

export interface EventoPayload {
  nome: string
  descricao: string
  tipo: TipoEvento
  dataInicio: string
  dataFim: string
  prazoAlteracao: string
}

export function listarEventos(params: {
  status?: StatusEvento | ''
  papel?: PapelEvento | ''
  page?: number
}) {
  const query = new URLSearchParams()
  if (params.status) query.set('status', params.status)
  if (params.papel) query.set('papel', params.papel)
  query.set('page', String(params.page ?? 0))
  query.set('size', '20')
  query.set('sort', 'dataInicio,desc')
  return request<Pagina<EventoResumo>>(`/eventos?${query.toString()}`)
}

export function criarEvento(body: EventoPayload) {
  return request<EventoDetalhe>('/eventos', { method: 'POST', body })
}

export function buscarEvento(id: number) {
  return request<EventoDetalhe>(`/eventos/${id}`)
}

export function atualizarEvento(id: number, body: EventoPayload) {
  return request<EventoDetalhe>(`/eventos/${id}`, { method: 'PUT', body })
}

export function excluirEvento(id: number) {
  return request<void>(`/eventos/${id}`, { method: 'DELETE' })
}

export function publicarEvento(id: number) {
  return request<EventoDetalhe>(`/eventos/${id}/publicar`, { method: 'POST' })
}

export function encerrarEvento(id: number) {
  return request<EventoDetalhe>(`/eventos/${id}/encerrar`, { method: 'POST' })
}

export function reabrirEvento(id: number, body: { prazoAlteracao: string; dataInicio?: string; dataFim?: string }) {
  return request<EventoDetalhe>(`/eventos/${id}/reabrir`, { method: 'POST', body })
}

export function concluirEvento(id: number) {
  return request<EventoDetalhe>(`/eventos/${id}/concluir`, { method: 'POST' })
}

export function cancelarEvento(id: number) {
  return request<EventoDetalhe>(`/eventos/${id}/cancelar`, { method: 'POST' })
}
