import type { StatusEvento, TipoEvento } from '../types/api'

export const LABEL_TIPO: Record<TipoEvento, string> = {
  CHURRASCO: 'Churrasco',
  ALMOCO: 'Almoço',
  JANTAR: 'Jantar',
  CAFE_DA_MANHA: 'Café da manhã',
  LANCHE: 'Lanche',
  FEIJOADA: 'Feijoada',
  PIQUENIQUE: 'Piquenique',
  FESTA_TEMATICA: 'Festa temática',
  CONFRATERNIZACAO: 'Confraternização',
  OUTRO: 'Outro',
}

export const LABEL_STATUS: Record<StatusEvento, string> = {
  RASCUNHO: 'Rascunho',
  ABERTO: 'Aberto',
  ENCERRADO: 'Encerrado',
  REALIZADO: 'Realizado',
  CANCELADO: 'Cancelado',
}

export const TIPOS_EVENTO = Object.keys(LABEL_TIPO) as TipoEvento[]
export const STATUS_EVENTO = Object.keys(LABEL_STATUS) as StatusEvento[]
