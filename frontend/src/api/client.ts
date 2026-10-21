import type { CampoErro } from '../types/api'

export const TOKEN_KEY = 'mesa.token'
export const LOGOUT_EVENT = 'mesa:logout'

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'

export class ApiError extends Error {
  readonly status: number
  readonly title: string
  readonly detail: string
  readonly erros: CampoErro[]

  constructor(status: number, title: string, detail: string, erros: CampoErro[] = []) {
    super(detail)
    this.name = 'ApiError'
    this.status = status
    this.title = title
    this.detail = detail
    this.erros = erros
  }
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  window.dispatchEvent(new Event(LOGOUT_EVENT))
}

interface RequestOptions {
  method?: string
  body?: unknown
  auth?: boolean
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers()
  if (options.body !== undefined) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getToken()
  if (options.auth !== false && token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  if (response.status === 401 && options.auth !== false) {
    clearToken()
  }

  if (response.status === 204) {
    return undefined as T
  }

  const texto = await response.text()
  const json = texto ? (JSON.parse(texto) as Record<string, unknown>) : {}

  if (!response.ok) {
    const erros = Array.isArray(json.erros) ? (json.erros as CampoErro[]) : []
    throw new ApiError(
      response.status,
      typeof json.title === 'string' ? json.title : 'Erro',
      typeof json.detail === 'string' ? json.detail : 'Não foi possível concluir a operação.',
      erros,
    )
  }

  return json as T
}
