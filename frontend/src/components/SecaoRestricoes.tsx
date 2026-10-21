import { useEffect, useState } from 'react'
import { buscarRestricoesEvento } from '../api/restricoes'
import type { EventoDetalhe, RestricoesEvento } from '../types/api'
import ErroApi from './ErroApi'

export default function SecaoRestricoes({ evento }: { evento: EventoDetalhe }) {
  const [dados, setDados] = useState<RestricoesEvento | null>(null)
  const [erro, setErro] = useState<unknown>(null)

  useEffect(() => {
    void buscarRestricoesEvento(evento.id).then(setDados).catch(setErro)
  }, [evento.id, evento.estouParticipando])

  return (
    <section className="secao card">
      <h2>Restrições e alérgenos</h2>
      <p>Cruzamento entre o que as pessoas não podem comer e o que está na lista de itens.</p>
      <ErroApi erro={erro} />
      {!dados && !erro && <p className="vazio">Analisando…</p>}
      {dados && (
        <>
          {dados.resumo.length === 0 && <p className="vazio">Nenhuma restrição entre os participantes.</p>}
          {dados.resumo.length > 0 && (
            <ul>
              {dados.resumo.map((item) => (
                <li key={item.restricaoId}>
                  {item.nome}: {item.quantidadeParticipantes} participante(s)
                </li>
              ))}
            </ul>
          )}
          {dados.porItem.length > 0 && (
            <div className="secao">
              <h3>Itens que afetam alguém</h3>
              {dados.porItem.map((item) => (
                <div key={item.itemId} className="alerta alerta-aviso">
                  <strong>{item.nome}</strong>
                  <p>
                    {item.quantidadeAfetados} pessoa(s): {item.participantes.map((pessoa) => pessoa.nome).join(', ')}
                  </p>
                </div>
              ))}
            </div>
          )}
          {dados.semItemCompativel.length > 0 && (
            <div className="alerta alerta-erro">
              <strong>Restrições sem item compatível</strong>
              <ul>
                {dados.semItemCompativel.map((item) => (
                  <li key={item.restricaoId}>
                    {item.nome} ({item.quantidadeParticipantes})
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
