import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { criarEvento } from '../api/eventos'
import ErroApi from '../components/ErroApi'
import EventoForm, { type EventoFormValores } from '../components/EventoForm'
import { localParaInstant } from '../utils/formatar'

const vazio: EventoFormValores = {
  nome: '',
  descricao: '',
  tipo: 'ALMOCO',
  dataInicio: '',
  dataFim: '',
  prazoAlteracao: '',
}

export default function EventoNovo() {
  const navigate = useNavigate()
  const [valores, setValores] = useState(vazio)
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)

  async function salvar() {
    setErro(null)
    setEnviando(true)
    try {
      const criado = await criarEvento({
        nome: valores.nome,
        descricao: valores.descricao,
        tipo: valores.tipo,
        dataInicio: localParaInstant(valores.dataInicio),
        dataFim: localParaInstant(valores.dataFim),
        prazoAlteracao: localParaInstant(valores.prazoAlteracao),
      })
      navigate(`/eventos/${criado.id}`)
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="card form-card">
      <p className="eyebrow">Organizar</p>
      <h1>Novo evento</h1>
      <p>O evento nasce em rascunho. Publique depois de cadastrar pelo menos um item.</p>
      <ErroApi erro={erro} />
      <EventoForm valores={valores} onChange={setValores} onSubmit={salvar} enviando={enviando} submitLabel="Criar rascunho" />
    </section>
  )
}
