import { useEffect, useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { listarRestricoes } from '../api/catalogos'
import { useAuth } from '../auth/AuthContext'
import ErroApi from '../components/ErroApi'
import SelecaoRestricoes from '../components/SelecaoRestricoes'
import type { RestricaoCatalogo } from '../types/api'
import { formatarCpfInput, somenteDigitos } from '../utils/mascararCpf'

export default function Cadastro() {
  const { usuario, carregando, cadastrar } = useAuth()
  const navigate = useNavigate()
  const [nome, setNome] = useState('')
  const [cpf, setCpf] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [restricaoIds, setRestricaoIds] = useState<number[]>([])
  const [restricoes, setRestricoes] = useState<RestricaoCatalogo[]>([])
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    void listarRestricoes().then(setRestricoes).catch(setErro)
  }, [])

  if (!carregando && usuario) {
    return <Navigate to="/" replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      await cadastrar({
        nome,
        cpf: somenteDigitos(cpf),
        email,
        senha,
        restricaoIds,
      })
      navigate('/', { replace: true })
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="card auth-card">
      <p className="eyebrow">Nova conta</p>
      <h1>Cadastro</h1>
      <p>O CPF precisa ser válido. E-mail e CPF não mudam depois.</p>
      <ErroApi erro={erro} />
      <form onSubmit={enviar}>
        <label className="campo">
          Nome
          <input value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={150} />
        </label>
        <label className="campo">
          CPF
          <input
            value={cpf}
            onChange={(e) => setCpf(formatarCpfInput(e.target.value))}
            inputMode="numeric"
            autoComplete="off"
            required
          />
        </label>
        <label className="campo">
          E-mail
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label className="campo">
          Senha
          <span className="ajuda">Mínimo de 8 caracteres.</span>
          <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} minLength={8} required />
        </label>
        <p>Restrições alimentares</p>
        <SelecaoRestricoes restricoes={restricoes} selecionadas={restricaoIds} onChange={setRestricaoIds} />
        <div className="acoes">
          <button type="submit" disabled={enviando}>
            {enviando ? 'Criando…' : 'Criar conta'}
          </button>
          <Link className="botao secundario" to="/login">
            Já tenho conta
          </Link>
        </div>
      </form>
    </section>
  )
}
