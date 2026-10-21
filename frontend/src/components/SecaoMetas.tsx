import { useEffect, useState, type FormEvent } from 'react'
import { listarCategorias } from '../api/catalogos'
import { criarMeta, excluirMeta, listarMetas } from '../api/metas'
import ErroApi from './ErroApi'
import type { CatalogoItem, EventoDetalhe, MetaCategoria } from '../types/api'

export default function SecaoMetas({ evento }: { evento: EventoDetalhe }) {
  const [metas, setMetas] = useState<MetaCategoria[]>([])
  const [categorias, setCategorias] = useState<CatalogoItem[]>([])
  const [categoriaId, setCategoriaId] = useState<number | ''>('')
  const [quantidade, setQuantidade] = useState(1)
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)
  const gerenciavel = evento.souOrganizador && (evento.status === 'RASCUNHO' || evento.status === 'ABERTO')

  async function carregar() {
    const [lista, cats] = await Promise.all([listarMetas(evento.id), listarCategorias()])
    setMetas(lista)
    setCategorias(cats)
  }

  useEffect(() => {
    void carregar().catch(setErro)
  }, [evento.id])

  async function adicionar(form: FormEvent) {
    form.preventDefault()
    if (categoriaId === '') return
    setErro(null)
    setEnviando(true)
    try {
      await criarMeta(evento.id, { categoriaId, quantidadeMeta: quantidade })
      setQuantidade(1)
      await carregar()
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  async function remover(metaId: number) {
    setErro(null)
    try {
      await excluirMeta(evento.id, metaId)
      await carregar()
    } catch (falha) {
      setErro(falha)
    }
  }

  return (
    <section className="secao card">
      <h2>Metas por categoria</h2>
      <p>Só é possível criar meta se já existir item daquela categoria.</p>
      <ErroApi erro={erro} />
      {metas.length === 0 && <p className="vazio">Nenhuma meta definida.</p>}
      <ul>
        {metas.map((meta) => (
          <li key={meta.id} className="meta-linha" style={{ marginBottom: '0.6rem' }}>
            <strong>{meta.categoria.nome}</strong>
            <span>meta {meta.quantidadeMeta}</span>
            {gerenciavel && (
              <button type="button" className="secundario" onClick={() => void remover(meta.id)}>
                Remover
              </button>
            )}
          </li>
        ))}
      </ul>
      {gerenciavel && (
        <form onSubmit={adicionar}>
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
              Quantidade da meta
              <input
                type="number"
                min={1}
                value={quantidade}
                onChange={(e) => setQuantidade(Number(e.target.value))}
                required
              />
            </label>
          </div>
          <div className="acoes">
            <button type="submit" disabled={enviando}>
              {enviando ? 'Salvando…' : 'Definir meta'}
            </button>
          </div>
        </form>
      )}
    </section>
  )
}
