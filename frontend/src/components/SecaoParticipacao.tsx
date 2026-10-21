import { useEffect, useState } from 'react'
import { cancelarParticipacao, listarParticipacoes, participar } from '../api/participacoes'
import { useAuth } from '../auth/AuthContext'
import type { EventoDetalhe, Participacao } from '../types/api'
import ErroApi from './ErroApi'

export default function SecaoParticipacao({
  evento,
  onAtualizou,
}: {
  evento: EventoDetalhe
  onAtualizou: () => Promise<void>
}) {
  const { usuario } = useAuth()
  const [lista, setLista] = useState<Participacao[]>([])
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)

  async function carregar() {
    setLista(await listarParticipacoes(evento.id))
  }

  useEffect(() => {
    void carregar().catch(setErro)
  }, [evento.id, evento.estouParticipando])

  const minha = lista.find((item) => item.usuarioId === usuario?.id)

  async function entrar() {
    setErro(null)
    setEnviando(true)
    try {
      await participar(evento.id)
      await carregar()
      await onAtualizou()
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  async function sair() {
    if (!minha) return
    setErro(null)
    setEnviando(true)
    try {
      await cancelarParticipacao(evento.id, minha.id)
      await carregar()
      await onAtualizou()
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="secao card">
      <h2>Participantes</h2>
      <ErroApi erro={erro} />
      {evento.status === 'ABERTO' && !evento.estouParticipando && evento.aceitaAlteracoes && (
        <div className="acoes">
          <button type="button" disabled={enviando} onClick={() => void entrar()}>
            {enviando ? 'Confirmando…' : 'Quero participar'}
          </button>
        </div>
      )}
      {minha && evento.aceitaAlteracoes && (
        <div className="acoes">
          <button type="button" className="secundario" disabled={enviando} onClick={() => void sair()}>
            Cancelar minha participação
          </button>
        </div>
      )}
      {lista.length === 0 && <p className="vazio">Ninguém confirmou presença ainda.</p>}
      <table className="tabela">
        <thead>
          <tr>
            <th>Nome</th>
            <th>CPF</th>
            <th>Restrições</th>
            {evento.souOrganizador && evento.aceitaAlteracoes && <th></th>}
          </tr>
        </thead>
        <tbody>
          {lista.map((participacao) => (
            <tr key={participacao.id}>
              <td>{participacao.nome}</td>
              <td>{participacao.cpf}</td>
              <td>{participacao.restricoes.map((item) => item.nome).join(', ') || '—'}</td>
              {evento.souOrganizador && evento.aceitaAlteracoes && (
                <td>
                  <button
                    type="button"
                    className="secundario"
                    onClick={() => void cancelarParticipacao(evento.id, participacao.id).then(carregar).then(onAtualizou).catch(setErro)}
                  >
                    Remover
                  </button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
