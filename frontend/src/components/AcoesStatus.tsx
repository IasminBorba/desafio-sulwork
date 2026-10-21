import { useState } from 'react'
import type { EventoDetalhe } from '../types/api'

export type AcaoStatus = 'publicar' | 'encerrar' | 'reabrir' | 'concluir' | 'cancelar' | 'excluir'

export default function AcoesStatus({
  evento,
  onAcao,
  enviando,
}: {
  evento: EventoDetalhe
  onAcao: (acao: AcaoStatus, extra?: { prazoAlteracao: string; dataInicio?: string; dataFim?: string }) => Promise<void>
  enviando: boolean
}) {
  const [prazo, setPrazo] = useState('')
  const [inicio, setInicio] = useState('')
  const [fim, setFim] = useState('')

  if (!evento.souOrganizador) {
    return null
  }

  return (
    <div className="acoes" data-testid="acoes-status">
      {evento.status === 'RASCUNHO' && (
        <>
          <button type="button" disabled={enviando} onClick={() => void onAcao('publicar')}>
            Publicar
          </button>
          <button type="button" className="perigo" disabled={enviando} onClick={() => void onAcao('excluir')}>
            Excluir rascunho
          </button>
        </>
      )}
      {evento.status === 'ABERTO' && (
        <button type="button" disabled={enviando} onClick={() => void onAcao('encerrar')}>
          Encerrar contribuições
        </button>
      )}
      {evento.status === 'ENCERRADO' && (
        <div className="card" style={{ width: '100%' }}>
          <h3>Reabrir</h3>
          <label className="campo">
            Novo prazo
            <input type="datetime-local" value={prazo} onChange={(e) => setPrazo(e.target.value)} required />
          </label>
          <label className="campo">
            Novo início <span className="ajuda">Obrigatório se a data original já passou.</span>
            <input type="datetime-local" value={inicio} onChange={(e) => setInicio(e.target.value)} />
          </label>
          <label className="campo">
            Novo fim
            <input type="datetime-local" value={fim} onChange={(e) => setFim(e.target.value)} />
          </label>
          <div className="acoes">
            <button
              type="button"
              disabled={enviando || !prazo}
              onClick={() =>
                void onAcao('reabrir', {
                  prazoAlteracao: prazo,
                  dataInicio: inicio || undefined,
                  dataFim: fim || undefined,
                })
              }
            >
              Reabrir
            </button>
            <button type="button" className="secundario" disabled={enviando} onClick={() => void onAcao('concluir')}>
              Marcar como realizado
            </button>
          </div>
        </div>
      )}
      {(evento.status === 'RASCUNHO' || evento.status === 'ABERTO' || evento.status === 'ENCERRADO') && (
        <button type="button" className="perigo" disabled={enviando} onClick={() => void onAcao('cancelar')}>
          Cancelar evento
        </button>
      )}
    </div>
  )
}
