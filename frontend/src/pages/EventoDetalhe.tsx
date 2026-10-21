import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  atualizarEvento,
  buscarEvento,
  cancelarEvento,
  concluirEvento,
  encerrarEvento,
  excluirEvento,
  publicarEvento,
  reabrirEvento,
} from '../api/eventos'
import AcoesStatus, { type AcaoStatus } from '../components/AcoesStatus'
import ErroApi from '../components/ErroApi'
import EventoForm, { type EventoFormValores } from '../components/EventoForm'
import StatusBadge from '../components/StatusBadge'
import type { EventoDetalhe as Evento } from '../types/api'
import { formatarData, instantParaLocal, localParaInstant } from '../utils/formatar'
import { LABEL_TIPO } from '../utils/labels'

export default function EventoDetalhe() {
  const { id } = useParams()
  const eventoId = Number(id)
  const navigate = useNavigate()
  const [evento, setEvento] = useState<Evento | null>(null)
  const [valores, setValores] = useState<EventoFormValores | null>(null)
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)

  const carregar = useCallback(async () => {
    const detalhe = await buscarEvento(eventoId)
    setEvento(detalhe)
    setValores({
      nome: detalhe.nome,
      descricao: detalhe.descricao,
      tipo: detalhe.tipo,
      dataInicio: instantParaLocal(detalhe.dataInicio),
      dataFim: instantParaLocal(detalhe.dataFim),
      prazoAlteracao: instantParaLocal(detalhe.prazoAlteracao),
    })
  }, [eventoId])

  useEffect(() => {
    setErro(null)
    void carregar().catch(setErro)
  }, [carregar])

  async function salvarEdicao() {
    if (!valores) return
    setErro(null)
    setEnviando(true)
    try {
      const atualizado = await atualizarEvento(eventoId, {
        nome: valores.nome,
        descricao: valores.descricao,
        tipo: valores.tipo,
        dataInicio: localParaInstant(valores.dataInicio),
        dataFim: localParaInstant(valores.dataFim),
        prazoAlteracao: localParaInstant(valores.prazoAlteracao),
      })
      setEvento(atualizado)
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  async function executar(acao: AcaoStatus, extra?: { prazoAlteracao: string; dataInicio?: string; dataFim?: string }) {
    setErro(null)
    setEnviando(true)
    try {
      if (acao === 'excluir') {
        await excluirEvento(eventoId)
        navigate('/')
        return
      }
      const mapa = {
        publicar: () => publicarEvento(eventoId),
        encerrar: () => encerrarEvento(eventoId),
        concluir: () => concluirEvento(eventoId),
        cancelar: () => cancelarEvento(eventoId),
        reabrir: () =>
          reabrirEvento(eventoId, {
            prazoAlteracao: localParaInstant(extra?.prazoAlteracao ?? ''),
            dataInicio: extra?.dataInicio ? localParaInstant(extra.dataInicio) : undefined,
            dataFim: extra?.dataFim ? localParaInstant(extra.dataFim) : undefined,
          }),
      } as const
      setEvento(await mapa[acao]())
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  if (!evento || !valores) {
    return erro ? <ErroApi erro={erro} /> : <p className="vazio">Carregando evento…</p>
  }

  const gerenciavel = evento.souOrganizador && (evento.status === 'RASCUNHO' || evento.status === 'ABERTO')

  return (
    <section>
      <div className="page-head">
        <div>
          <p className="eyebrow">{LABEL_TIPO[evento.tipo]}</p>
          <h1>{evento.nome}</h1>
          <div className="meta-linha">
            <StatusBadge status={evento.status} />
            <span>Início {formatarData(evento.dataInicio)}</span>
            <span>Prazo {formatarData(evento.prazoAlteracao)}</span>
            <span>Organiza {evento.organizador.nome}</span>
          </div>
        </div>
        {evento.souOrganizador && (
          <Link className="botao secundario" to={`/eventos/${evento.id}/painel`}>
            Painel
          </Link>
        )}
      </div>
      <ErroApi erro={erro} />
      <div className="card">
        <p>{evento.descricao}</p>
        <AcoesStatus evento={evento} onAcao={executar} enviando={enviando} />
      </div>
      {gerenciavel && (
        <section className="secao card">
          <h2>Editar dados</h2>
          <EventoForm
            valores={valores}
            onChange={setValores}
            onSubmit={salvarEdicao}
            enviando={enviando}
            submitLabel="Salvar"
          />
        </section>
      )}
    </section>
  )
}
