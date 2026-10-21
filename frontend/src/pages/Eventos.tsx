import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarEventos } from '../api/eventos'
import StatusBadge from '../components/StatusBadge'
import ErroApi from '../components/ErroApi'
import type { EventoResumo, PapelEvento, StatusEvento } from '../types/api'
import { formatarData } from '../utils/formatar'
import { LABEL_STATUS, LABEL_TIPO, STATUS_EVENTO } from '../utils/labels'

type Aba = '' | PapelEvento

export default function Eventos() {
  const [aba, setAba] = useState<Aba>('')
  const [status, setStatus] = useState<StatusEvento | ''>('')
  const [eventos, setEventos] = useState<EventoResumo[]>([])
  const [erro, setErro] = useState<unknown>(null)
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    setCarregando(true)
    setErro(null)
    void listarEventos({ papel: aba, status })
      .then((pagina) => setEventos(pagina.content))
      .catch(setErro)
      .finally(() => setCarregando(false))
  }, [aba, status])

  return (
    <section>
      <div className="page-head">
        <div>
          <p className="eyebrow">Agenda</p>
          <h1>Eventos</h1>
          <p>O que está na mesa, o que você organiza e onde você já confirmou presença.</p>
        </div>
        <Link className="botao" to="/eventos/novo">
          Novo evento
        </Link>
      </div>

      <div className="tabs">
        <button type="button" className={`chip ${aba === '' ? 'ativo' : ''}`} onClick={() => setAba('')}>
          Visíveis
        </button>
        <button type="button" className={`chip ${aba === 'ORGANIZADOR' ? 'ativo' : ''}`} onClick={() => setAba('ORGANIZADOR')}>
          Organizando
        </button>
        <button type="button" className={`chip ${aba === 'PARTICIPANTE' ? 'ativo' : ''}`} onClick={() => setAba('PARTICIPANTE')}>
          Participando
        </button>
      </div>
      <div className="chips" style={{ marginBottom: '1rem' }}>
        <button type="button" className={`chip ${status === '' ? 'ativo' : ''}`} onClick={() => setStatus('')}>
          Todos os status
        </button>
        {STATUS_EVENTO.map((item) => (
          <button
            key={item}
            type="button"
            className={`chip ${status === item ? 'ativo' : ''}`}
            onClick={() => setStatus(item)}
          >
            {LABEL_STATUS[item]}
          </button>
        ))}
      </div>

      <ErroApi erro={erro} />
      {carregando && <p className="vazio">Carregando eventos…</p>}
      {!carregando && eventos.length === 0 && <p className="vazio">Nenhum evento nesta lista.</p>}

      <div className="grid grid-3">
        {eventos.map((evento) => (
          <Link key={evento.id} className="card evento-card" to={`/eventos/${evento.id}`}>
            <div className="meta-linha">
              <StatusBadge status={evento.status} />
              <span>{LABEL_TIPO[evento.tipo]}</span>
            </div>
            <h2>{evento.nome}</h2>
            <p>{formatarData(evento.dataInicio)}</p>
            {evento.souOrganizador && <small>Você organiza</small>}
          </Link>
        ))}
      </div>
    </section>
  )
}
