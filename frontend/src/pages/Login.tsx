import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import ErroApi from '../components/ErroApi'
import { useAuth } from '../auth/AuthContext'

export default function Login() {
  const { usuario, carregando, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const destino = (location.state as { from?: string } | null)?.from ?? '/'
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)

  if (!carregando && usuario) {
    return <Navigate to={destino} replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      await login(email, senha)
      navigate(destino, { replace: true })
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="card auth-card">
      <p className="eyebrow">Acesso</p>
      <h1>Entrar na Mesa</h1>
      <p>Use a conta da equipe ou uma das contas de demonstração.</p>
      <ErroApi erro={erro} />
      <form onSubmit={enviar}>
        <label className="campo">
          E-mail
          <input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label className="campo">
          Senha
          <input
            type="password"
            autoComplete="current-password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />
        </label>
        <div className="acoes">
          <button type="submit" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
          <Link className="botao secundario" to="/cadastro">
            Criar conta
          </Link>
        </div>
      </form>
      <p className="ajuda" style={{ marginTop: '1.2rem', fontSize: '0.9rem' }}>
        Demo: <code>ana.souza@sulwork.demo</code> · senha <code>senha123</code>
      </p>
    </section>
  )
}
