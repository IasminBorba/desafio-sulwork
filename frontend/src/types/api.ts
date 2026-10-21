export type StatusEvento = 'RASCUNHO' | 'ABERTO' | 'ENCERRADO' | 'REALIZADO' | 'CANCELADO'
export type TipoEvento =
  | 'CHURRASCO'
  | 'ALMOCO'
  | 'JANTAR'
  | 'CAFE_DA_MANHA'
  | 'LANCHE'
  | 'FEIJOADA'
  | 'PIQUENIQUE'
  | 'FESTA_TEMATICA'
  | 'CONFRATERNIZACAO'
  | 'OUTRO'
export type PapelEvento = 'ORGANIZADOR' | 'PARTICIPANTE'

export interface CatalogoItem {
  id: number
  nome: string
}

export interface RestricaoCatalogo {
  id: number
  nome: string
  alergenos: CatalogoItem[]
}

export interface RestricaoResumo {
  id: number
  nome: string
}

export interface Usuario {
  id: number
  nome: string
  cpf: string
  email: string
  restricoes: RestricaoResumo[]
}

export interface LoginResponse {
  token: string
  expiraEm: string
}

export interface OrganizadorResumo {
  id: number
  nome: string
}

export interface EventoResumo {
  id: number
  nome: string
  tipo: TipoEvento
  status: StatusEvento
  dataInicio: string
  dataFim: string
  prazoAlteracao: string
  souOrganizador: boolean
}

export interface EventoDetalhe {
  id: number
  nome: string
  descricao: string
  tipo: TipoEvento
  status: StatusEvento
  dataInicio: string
  dataFim: string
  prazoAlteracao: string
  organizador: OrganizadorResumo
  aceitaAlteracoes: boolean
  souOrganizador: boolean
  estouParticipando: boolean
}

export interface Pagina<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface ItemEvento {
  id: number
  nome: string
  categoria: CatalogoItem
  quantidadeNecessaria: number
  obrigatorio: boolean
  alergenos: CatalogoItem[]
}

export interface MetaCategoria {
  id: number
  categoria: CatalogoItem
  quantidadeMeta: number
}

export interface Participacao {
  id: number
  usuarioId: number
  nome: string
  cpf: string
  restricoes: RestricaoResumo[]
}

export interface Contribuicao {
  id: number
  itemId: number
  itemNome: string
  quantidade: number
  participacaoId: number
  usuarioId: number
  usuarioNome: string
}

export interface CoberturaItem {
  itemId: number
  nome: string
  categoriaNome: string
  obrigatorio: boolean
  quantidadeNecessaria: number
  contribuido: number
  faltante: number
  percentual: number
}

export interface CoberturaCategoria {
  categoriaId: number
  categoriaNome: string
  quantidadeMeta: number
  contribuido: number
  percentual: number
}

export interface Cobertura {
  percentualGeral: number
  a: number
  b: number | null
  c: number | null
  categorias: CoberturaCategoria[]
  itens: CoberturaItem[]
  obrigatoriosPendentes: CoberturaItem[]
}

export interface ParticipanteAfetado {
  usuarioId: number
  nome: string
}

export interface ItemRestricao {
  itemId: number
  nome: string
  quantidadeAfetados: number
  participantes: ParticipanteAfetado[]
}

export interface RestricaoResumoEvento {
  restricaoId: number
  nome: string
  quantidadeParticipantes: number
}

export interface RestricaoSemItem {
  restricaoId: number
  nome: string
  quantidadeParticipantes: number
}

export interface RestricoesEvento {
  resumo: RestricaoResumoEvento[]
  porItem: ItemRestricao[]
  semItemCompativel: RestricaoSemItem[]
}

export interface PainelContribuicaoItem {
  itemId: number
  itemNome: string
  categoriaNome: string
  quantidade: number
}

export interface PainelParticipante {
  participacaoId: number
  usuarioId: number
  nome: string
  cpf: string
  semContribuicao: boolean
  restricoes: RestricaoResumo[]
  contribuicoes: PainelContribuicaoItem[]
}

export interface Painel {
  evento: EventoDetalhe
  cobertura: Cobertura
  restricoes: RestricoesEvento
  participantes: PainelParticipante[]
}

export interface CampoErro {
  campo: string
  mensagem: string
}
