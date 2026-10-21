import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { usuario, logout } = useAuth()

  return (
    <div className="app">
      <header className="topbar">
        <Link className="brand" to="/">
          <span className="brand-mark" aria-hidden="true" />
          <span>
            <strong>Mesa</strong>
            <small>eventos Sulwork</small>
          </span>
        </Link>
        <nav className="nav">
          {usuario ? (
            <>
              <NavLink to="/">Eventos</NavLink>
              <NavLink to="/perfil">{usuario.nome.split(' ')[0]}</NavLink>
              <button type="button" className="linkish" onClick={logout}>
                Sair
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login">Entrar</NavLink>
              <NavLink to="/cadastro">Cadastrar</NavLink>
            </>
          )}
        </nav>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
