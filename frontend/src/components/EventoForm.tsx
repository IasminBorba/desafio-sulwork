import type { FormEvent } from 'react'
import type { TipoEvento } from '../types/api'
import { LABEL_TIPO, TIPOS_EVENTO } from '../utils/labels'

export interface EventoFormValores {
  nome: string
  descricao: string
  tipo: TipoEvento
  dataInicio: string
  dataFim: string
  prazoAlteracao: string
}

export default function EventoForm({
  valores,
  onChange,
  onSubmit,
  enviando,
  submitLabel,
}: {
  valores: EventoFormValores
  onChange: (valores: EventoFormValores) => void
  onSubmit: () => Promise<void> | void
  enviando: boolean
  submitLabel: string
}) {
  function alterar<K extends keyof EventoFormValores>(campo: K, valor: EventoFormValores[K]) {
    onChange({ ...valores, [campo]: valor })
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    await onSubmit()
  }

  return (
    <form onSubmit={enviar}>
      <label className="campo">
        Nome
        <input value={valores.nome} onChange={(e) => alterar('nome', e.target.value)} required maxLength={150} />
      </label>
      <label className="campo">
        Descrição
        <textarea value={valores.descricao} onChange={(e) => alterar('descricao', e.target.value)} required />
      </label>
      <label className="campo">
        Tipo
        <select value={valores.tipo} onChange={(e) => alterar('tipo', e.target.value as TipoEvento)}>
          {TIPOS_EVENTO.map((tipo) => (
            <option key={tipo} value={tipo}>
              {LABEL_TIPO[tipo]}
            </option>
          ))}
        </select>
      </label>
      <div className="grid grid-2">
        <label className="campo">
          Início
          <input type="datetime-local" value={valores.dataInicio} onChange={(e) => alterar('dataInicio', e.target.value)} required />
        </label>
        <label className="campo">
          Fim
          <input type="datetime-local" value={valores.dataFim} onChange={(e) => alterar('dataFim', e.target.value)} required />
        </label>
      </div>
      <label className="campo">
        Prazo para contribuições
        <input
          type="datetime-local"
          value={valores.prazoAlteracao}
          onChange={(e) => alterar('prazoAlteracao', e.target.value)}
          required
        />
      </label>
      <div className="acoes">
        <button type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : submitLabel}
        </button>
      </div>
    </form>
  )
}
