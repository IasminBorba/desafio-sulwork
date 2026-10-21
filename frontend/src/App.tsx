import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Cadastro from './pages/Cadastro'
import EventoDetalhe from './pages/EventoDetalhe'
import EventoNovo from './pages/EventoNovo'
import Eventos from './pages/Eventos'
import Login from './pages/Login'
import PainelEvento from './pages/PainelEvento'
import Perfil from './pages/Perfil'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Eventos />} />
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="/perfil" element={<Perfil />} />
          <Route path="/eventos/novo" element={<EventoNovo />} />
          <Route path="/eventos/:id" element={<EventoDetalhe />} />
          <Route path="/eventos/:id/painel" element={<PainelEvento />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
