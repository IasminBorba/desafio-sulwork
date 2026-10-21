import type { MetaCategoria } from '../types/api'
import { request } from './client'

export function listarMetas(eventoId: number) {
  return request<MetaCategoria[]>(`/eventos/${eventoId}/metas`)
}

export function criarMeta(eventoId: number, body: { categoriaId: number; quantidadeMeta: number }) {
  return request<MetaCategoria>(`/eventos/${eventoId}/metas`, { method: 'POST', body })
}

export function atualizarMeta(eventoId: number, metaId: number, body: { categoriaId: number; quantidadeMeta: number }) {
  return request<MetaCategoria>(`/eventos/${eventoId}/metas/${metaId}`, { method: 'PUT', body })
}

export function excluirMeta(eventoId: number, metaId: number) {
  return request<void>(`/eventos/${eventoId}/metas/${metaId}`, { method: 'DELETE' })
}
