import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { usuario, logout } = useAuth()

  return (
    <div className="app">
      <header className="topbar">
        <div className="topbar-inicio">
          <div className="brand">
            <span className="brand-mark" aria-hidden="true" />
            <span>
              <strong>Mesa</strong>
              <small>eventos Sulwork</small>
            </span>
          </div>
          {usuario ? (
            <NavLink to="/eventos">Eventos</NavLink>
          ) : (
            <NavLink to="/">Início</NavLink>
          )}
        </div>
        <nav className="nav">
          {usuario ? (
            <>
              <NavLink to="/perfil">Perfil</NavLink>
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
