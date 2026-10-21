import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import ProtectedRoute from './auth/ProtectedRoute'
import Layout from './components/Layout'
import Cadastro from './pages/Cadastro'
import EventoDetalhe from './pages/EventoDetalhe'
import EventoNovo from './pages/EventoNovo'
import Eventos from './pages/Eventos'
import Inicio from './pages/Inicio'
import Login from './pages/Login'
import PainelEvento from './pages/PainelEvento'
import Perfil from './pages/Perfil'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Inicio />} />
            <Route path="/login" element={<Login />} />
            <Route path="/cadastro" element={<Cadastro />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/eventos" element={<Eventos />} />
              <Route path="/perfil" element={<Perfil />} />
              <Route path="/eventos/novo" element={<EventoNovo />} />
              <Route path="/eventos/:id" element={<EventoDetalhe />} />
              <Route path="/eventos/:id/painel" element={<PainelEvento />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
