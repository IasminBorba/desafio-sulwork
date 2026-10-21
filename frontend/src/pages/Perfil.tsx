import { useEffect, useState, type FormEvent } from 'react'
import { listarRestricoes } from '../api/catalogos'
import { useAuth } from '../auth/AuthContext'
import ErroApi from '../components/ErroApi'
import SelecaoRestricoes from '../components/SelecaoRestricoes'
import type { RestricaoCatalogo } from '../types/api'
import { mascararCpf } from '../utils/mascararCpf'

export default function Perfil() {
  const { usuario, atualizar } = useAuth()
  const [nome, setNome] = useState(usuario?.nome ?? '')
  const [restricaoIds, setRestricaoIds] = useState(usuario?.restricoes.map((item) => item.id) ?? [])
  const [restricoes, setRestricoes] = useState<RestricaoCatalogo[]>([])
  const [erro, setErro] = useState<unknown>(null)
  const [ok, setOk] = useState(false)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    void listarRestricoes().then(setRestricoes).catch(setErro)
  }, [])

  if (!usuario) {
    return null
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setOk(false)
    setEnviando(true)
    try {
      await atualizar({ nome, restricaoIds })
      setOk(true)
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="card" style={{ maxWidth: 560 }}>
      <p className="eyebrow">Conta</p>
      <h1>Perfil</h1>
      <p>E-mail e CPF ficam fixos. Nome e restrições podem mudar.</p>
      <ErroApi erro={erro} />
      {ok && <div className="alerta alerta-ok">Perfil atualizado.</div>}
      <form onSubmit={enviar}>
        <label className="campo">
          Nome
          <input value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={150} />
        </label>
        <label className="campo">
          E-mail
          <input value={usuario.email} disabled />
        </label>
        <label className="campo">
          CPF
          <input value={mascararCpf(usuario.cpf)} disabled />
        </label>
        <p>Restrições alimentares</p>
        <SelecaoRestricoes restricoes={restricoes} selecionadas={restricaoIds} onChange={setRestricaoIds} />
        <div className="acoes">
          <button type="submit" disabled={enviando}>
            {enviando ? 'Salvando…' : 'Salvar'}
          </button>
        </div>
      </form>
    </section>
  )
}
