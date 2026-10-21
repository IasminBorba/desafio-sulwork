import type { ItemEvento } from '../types/api'
import { request } from './client'

export interface ItemPayload {
  nome: string
  categoriaId: number
  quantidadeNecessaria: number
  obrigatorio: boolean
  alergenoIds: number[]
}

export function listarItens(eventoId: number) {
  return request<ItemEvento[]>(`/eventos/${eventoId}/itens`)
}

export function criarItem(eventoId: number, body: ItemPayload) {
  return request<ItemEvento>(`/eventos/${eventoId}/itens`, { method: 'POST', body })
}

export function atualizarItem(eventoId: number, itemId: number, body: ItemPayload) {
  return request<ItemEvento>(`/eventos/${eventoId}/itens/${itemId}`, { method: 'PUT', body })
}

export function excluirItem(eventoId: number, itemId: number) {
  return request<void>(`/eventos/${eventoId}/itens/${itemId}`, { method: 'DELETE' })
}
