import { useEffect, useState, type FormEvent } from 'react'
import { criarItem, excluirItem, listarItens } from '../api/itens'
import { listarAlergenos, listarCategorias } from '../api/catalogos'
import ErroApi from './ErroApi'
import type { CatalogoItem, EventoDetalhe, ItemEvento } from '../types/api'

export default function SecaoItens({ evento, onAtualizou }: { evento: EventoDetalhe; onAtualizou?: () => void }) {
  const [itens, setItens] = useState<ItemEvento[]>([])
  const [categorias, setCategorias] = useState<CatalogoItem[]>([])
  const [alergenos, setAlergenos] = useState<CatalogoItem[]>([])
  const [nome, setNome] = useState('')
  const [categoriaId, setCategoriaId] = useState<number | ''>('')
  const [quantidade, setQuantidade] = useState(1)
  const [obrigatorio, setObrigatorio] = useState(true)
  const [alergenoIds, setAlergenoIds] = useState<number[]>([])
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)
  const gerenciavel = evento.souOrganizador && (evento.status === 'RASCUNHO' || evento.status === 'ABERTO')

  async function carregar() {
    const [lista, cats, alers] = await Promise.all([
      listarItens(evento.id),
      listarCategorias(),
      listarAlergenos(),
    ])
    setItens(lista)
    setCategorias(cats)
    setAlergenos(alers)
  }

  useEffect(() => {
    void carregar().catch(setErro)
  }, [evento.id])

  function alternarAlergeno(id: number) {
    setAlergenoIds((atual) => (atual.includes(id) ? atual.filter((item) => item !== id) : [...atual, id]))
  }

  async function adicionar(form: FormEvent) {
    form.preventDefault()
    if (categoriaId === '') return
    setErro(null)
    setEnviando(true)
    try {
      await criarItem(evento.id, {
        nome,
        categoriaId,
        quantidadeNecessaria: quantidade,
        obrigatorio,
        alergenoIds,
      })
      setNome('')
      setQuantidade(1)
      setAlergenoIds([])
      await carregar()
      onAtualizou?.()
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  async function remover(itemId: number) {
    setErro(null)
    try {
      await excluirItem(evento.id, itemId)
      await carregar()
      onAtualizou?.()
    } catch (falha) {
      setErro(falha)
    }
  }

  return (
    <section className="secao card">
      <h2>Itens</h2>
      <p>O que precisa chegar na mesa, por categoria. A quantidade é inteira; a unidade vai no nome.</p>
      <ErroApi erro={erro} />
      {itens.length === 0 && <p className="vazio">Nenhum item ainda.</p>}
      {itens.length > 0 && (
        <table className="tabela">
          <thead>
            <tr>
              <th>Item</th>
              <th>Categoria</th>
              <th>Qtd</th>
              <th>Alérgenos</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {itens.map((item) => (
              <tr key={item.id}>
                <td>
                  {item.nome} {item.obrigatorio ? <span className="badge">obrigatório</span> : <span className="badge">opcional</span>}
                </td>
                <td>{item.categoria.nome}</td>
                <td>{item.quantidadeNecessaria}</td>
                <td>{item.alergenos.map((alergeno) => alergeno.nome).join(', ') || '—'}</td>
                <td>
                  {gerenciavel && (
                    <button type="button" className="secundario" onClick={() => void remover(item.id)}>
                      Remover
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {gerenciavel && (
        <form onSubmit={adicionar} style={{ marginTop: '1rem' }}>
          <h3>Adicionar item</h3>
          <label className="campo">
            Nome
            <input value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={150} />
          </label>
          <div className="grid grid-2">
            <label className="campo">
              Categoria
              <select value={categoriaId} onChange={(e) => setCategoriaId(Number(e.target.value))} required>
                <option value="">Selecione</option>
                {categorias.map((categoria) => (
                  <option key={categoria.id} value={categoria.id}>
                    {categoria.nome}
                  </option>
                ))}
              </select>
            </label>
            <label className="campo">
              Quantidade necessária
              <input
                type="number"
                min={1}
                value={quantidade}
                onChange={(e) => setQuantidade(Number(e.target.value))}
                required
              />
            </label>
          </div>
          <label>
            <input type="checkbox" checked={obrigatorio} onChange={(e) => setObrigatorio(e.target.checked)} /> Item
            obrigatório
          </label>
          <p style={{ marginTop: '1rem' }}>Alérgenos</p>
          <div className="lista-check">
            {alergenos.map((alergeno) => (
              <label key={alergeno.id}>
                <input
                  type="checkbox"
                  checked={alergenoIds.includes(alergeno.id)}
                  onChange={() => alternarAlergeno(alergeno.id)}
                />
                {alergeno.nome}
              </label>
            ))}
          </div>
          <div className="acoes">
            <button type="submit" disabled={enviando}>
              {enviando ? 'Adicionando…' : 'Adicionar item'}
            </button>
          </div>
        </form>
      )}
    </section>
  )
}
