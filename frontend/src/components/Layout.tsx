import { Link, NavLink, Outlet } from 'react-router-dom'

export default function Layout() {
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
          <NavLink to="/">Eventos</NavLink>
          <NavLink to="/perfil">Perfil</NavLink>
          <NavLink to="/login">Entrar</NavLink>
        </nav>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
