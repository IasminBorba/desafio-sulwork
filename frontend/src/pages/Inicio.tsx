import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Inicio() {
  const { usuario } = useAuth()

  return (
    <section className="inicio">
      <p className="eyebrow">Mesa</p>
      <h1>Combinar o que cada um leva, sem a lista se perder no grupo</h1>
      <p className="inicio-lead">
        Para churrasco da equipe, almoço de alinhamento ou ceia em família: o organizador monta o que precisa ter,
        cada pessoa diz o que vai levar, e todo mundo vê o que ainda falta — inclusive opções para quem tem restrição
        alimentar.
      </p>
      <div className="acoes">
        {usuario ? (
          <>
            <Link className="botao" to="/eventos">
              Ver eventos
            </Link>
            <Link className="botao secundario" to="/eventos/novo">
              Novo evento
            </Link>
          </>
        ) : (
          <>
            <Link className="botao" to="/cadastro">
              Criar conta
            </Link>
            <Link className="botao secundario" to="/login">
              Entrar
            </Link>
          </>
        )}
      </div>

      <div className="grid grid-3 inicio-blocos">
        <article className="card">
          <h2>Organizar</h2>
          <p>
            Crie o evento, defina itens e metas por categoria, marque alérgenos e publique quando a lista estiver pronta.
            Só quem organiza edita e muda o status.
          </p>
        </article>
        <article className="card">
          <h2>Participar</h2>
          <p>
            Entre no evento e registre o que vai levar. Várias pessoas podem dividir um item; a soma nunca passa do
            necessário. Depois do prazo, a lista trava.
          </p>
        </article>
        <article className="card">
          <h2>Acompanhar</h2>
          <p>
            A cobertura mostra o percentual geral, o que falta em cada item e os obrigatórios pendentes. Quem organiza
            vê, no painel, o que cada pessoa vai levar.
          </p>
        </article>
      </div>
    </section>
  )
}
