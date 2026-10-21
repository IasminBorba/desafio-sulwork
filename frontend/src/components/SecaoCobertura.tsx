import { useEffect, useState } from 'react'
import { buscarCobertura } from '../api/cobertura'
import type { Cobertura, EventoDetalhe } from '../types/api'
import BarraCobertura from './BarraCobertura'
import ErroApi from './ErroApi'

export default function SecaoCobertura({ evento }: { evento: EventoDetalhe }) {
  const [cobertura, setCobertura] = useState<Cobertura | null>(null)
  const [erro, setErro] = useState<unknown>(null)

  useEffect(() => {
    void buscarCobertura(evento.id).then(setCobertura).catch(setErro)
  }, [evento.id, evento.status, evento.estouParticipando])

  return (
    <section className="secao card">
      <h2>Cobertura</h2>
      <p>Geral (A 50% + B 30% + C 20%), com renormalização se não houver meta ou obrigatório.</p>
      <ErroApi erro={erro} />
      {!cobertura && !erro && <p className="vazio">Calculando…</p>}
      {cobertura && (
        <>
          <BarraCobertura percentual={cobertura.percentualGeral} rotulo="Cobertura geral" />
          <div className="grid grid-3" style={{ marginTop: '1rem' }}>
            <div className="card">
              <strong>A · itens</strong>
              <p>{Math.round(cobertura.a * 100)}%</p>
            </div>
            <div className="card">
              <strong>B · metas</strong>
              <p>{cobertura.b == null ? 'sem meta' : `${Math.round(cobertura.b * 100)}%`}</p>
            </div>
            <div className="card">
              <strong>C · obrigatórios</strong>
              <p>{cobertura.c == null ? 'sem obrigatório' : `${Math.round(cobertura.c * 100)}%`}</p>
            </div>
          </div>
          {cobertura.categorias.length > 0 && (
            <div className="secao">
              <h3>Por categoria</h3>
              {cobertura.categorias.map((categoria) => (
                <div key={categoria.categoriaId} style={{ marginBottom: '0.8rem' }}>
                  <BarraCobertura percentual={categoria.percentual} rotulo={categoria.categoriaNome} />
                </div>
              ))}
            </div>
          )}
          <div className="secao">
            <h3>Por item</h3>
            {cobertura.itens.map((item) => (
              <div key={item.itemId} style={{ marginBottom: '0.8rem' }}>
                <BarraCobertura
                  percentual={item.percentual}
                  rotulo={`${item.nome} · ${item.contribuido}/${item.quantidadeNecessaria}`}
                />
              </div>
            ))}
          </div>
          {cobertura.obrigatoriosPendentes.length > 0 && (
            <div className="alerta alerta-aviso">
              <strong>Obrigatórios pendentes</strong>
              <ul>
                {cobertura.obrigatoriosPendentes.map((item) => (
                  <li key={item.itemId}>
                    {item.nome} — faltam {item.faltante}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </>
      )}
    </section>
  )
}
