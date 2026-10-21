import type { CatalogoItem, RestricaoCatalogo } from '../types/api'
import { request } from './client'

export function listarCategorias() {
  return request<CatalogoItem[]>('/categorias', { auth: false })
}

export function listarAlergenos() {
  return request<CatalogoItem[]>('/alergenos', { auth: false })
}

export function listarRestricoes() {
  return request<RestricaoCatalogo[]>('/restricoes', { auth: false })
}
