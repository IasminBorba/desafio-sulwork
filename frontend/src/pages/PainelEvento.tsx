import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { buscarPainel } from '../api/painel'
import BarraCobertura from '../components/BarraCobertura'
import ErroApi from '../components/ErroApi'
import StatusBadge from '../components/StatusBadge'
import type { Painel } from '../types/api'
import { formatarData } from '../utils/formatar'
import { LABEL_TIPO } from '../utils/labels'

type Visao = 'participante' | 'item'

export default function PainelEvento() {
  const { id } = useParams()
  const eventoId = Number(id)
  const [painel, setPainel] = useState<Painel | null>(null)
  const [erro, setErro] = useState<unknown>(null)
  const [visao, setVisao] = useState<Visao>('participante')

  useEffect(() => {
    void buscarPainel(eventoId).then(setPainel).catch(setErro)
  }, [eventoId])

  const porItem = useMemo(() => {
    if (!painel) return []
    const mapa = new Map<number, { nome: string; categoria: string; pessoas: { nome: string; quantidade: number }[] }>()
    for (const participante of painel.participantes) {
      for (const contribuicao of participante.contribuicoes) {
        const atual = mapa.get(contribuicao.itemId) ?? {
          nome: contribuicao.itemNome,
          categoria: contribuicao.categoriaNome,
          pessoas: [],
        }
        atual.pessoas.push({ nome: participante.nome, quantidade: contribuicao.quantidade })
        mapa.set(contribuicao.itemId, atual)
      }
    }
    return [...mapa.entries()].map(([itemId, valor]) => ({ itemId, ...valor }))
  }, [painel])

  if (erro) {
    return (
      <section>
        <ErroApi erro={erro} />
        <Link to={`/eventos/${eventoId}`}>Voltar ao evento</Link>
      </section>
    )
  }
  if (!painel) {
    return <p className="vazio">Montando o painel…</p>
  }

  const semContribuicao = painel.participantes.filter((pessoa) => pessoa.semContribuicao)

  return (
    <section>
      <div className="page-head">
        <div>
          <p className="eyebrow">Painel do organizador</p>
          <h1>{painel.evento.nome}</h1>
          <div className="meta-linha">
            <StatusBadge status={painel.evento.status} />
            <span>{LABEL_TIPO[painel.evento.tipo]}</span>
            <span>{formatarData(painel.evento.dataInicio)}</span>
            <Link to={`/eventos/${eventoId}`}>Voltar ao evento</Link>
          </div>
        </div>
      </div>

      <div className="card">
        <BarraCobertura percentual={painel.cobertura.percentualGeral} rotulo="Cobertura geral" />
        <div className="grid grid-3" style={{ marginTop: '1rem' }}>
          <div>
            <strong>A</strong>
            <p>{Math.round(painel.cobertura.a * 100)}%</p>
          </div>
          <div>
            <strong>B</strong>
            <p>{painel.cobertura.b == null ? '—' : `${Math.round(painel.cobertura.b * 100)}%`}</p>
          </div>
          <div>
            <strong>C</strong>
            <p>{painel.cobertura.c == null ? '—' : `${Math.round(painel.cobertura.c * 100)}%`}</p>
          </div>
        </div>
      </div>

      {semContribuicao.length > 0 && (
        <div className="alerta alerta-aviso secao">
          <strong>Ainda não contribuíram</strong>
          <p>{semContribuicao.map((pessoa) => pessoa.nome).join(', ')}</p>
        </div>
      )}

      {painel.restricoes.porItem.length > 0 && (
        <div className="alerta alerta-aviso secao">
          <strong>Itens com conflito de restrição</strong>
          <ul>
            {painel.restricoes.porItem.map((item) => (
              <li key={item.itemId}>
                {item.nome}: {item.participantes.map((pessoa) => pessoa.nome).join(', ')}
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className="tabs secao">
        <button type="button" className={`chip ${visao === 'participante' ? 'ativo' : ''}`} onClick={() => setVisao('participante')}>
          Por participante
        </button>
        <button type="button" className={`chip ${visao === 'item' ? 'ativo' : ''}`} onClick={() => setVisao('item')}>
          Por item
        </button>
      </div>

      {visao === 'participante' &&
        painel.participantes.map((pessoa) => (
          <article key={pessoa.participacaoId} className={`card secao ${pessoa.semContribuicao ? 'destaque' : ''}`}>
            <h2>{pessoa.nome}</h2>
            <p>
              {pessoa.cpf}
              {pessoa.restricoes.length > 0 ? ` · ${pessoa.restricoes.map((item) => item.nome).join(', ')}` : ''}
            </p>
            {pessoa.semContribuicao ? (
              <p className="vazio">Ainda não registrou o que vai levar.</p>
            ) : (
              <ul>
                {pessoa.contribuicoes.map((item) => (
                  <li key={item.itemId}>
                    {item.quantidade}× {item.itemNome} ({item.categoriaNome})
                  </li>
                ))}
              </ul>
            )}
          </article>
        ))}

      {visao === 'item' &&
        porItem.map((item) => (
          <article key={item.itemId} className="card secao">
            <h2>{item.nome}</h2>
            <p>{item.categoria}</p>
            <ul>
              {item.pessoas.map((pessoa) => (
                <li key={pessoa.nome}>
                  {pessoa.nome}: {pessoa.quantidade}
                </li>
              ))}
            </ul>
          </article>
        ))}
    </section>
  )
}
