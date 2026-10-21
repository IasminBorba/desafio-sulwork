import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { criarContribuicao, excluirContribuicao, listarContribuicoes } from '../api/contribuicoes'
import { buscarCobertura } from '../api/cobertura'
import { listarItens } from '../api/itens'
import { listarRestricoes } from '../api/catalogos'
import { useAuth } from '../auth/AuthContext'
import type { CoberturaItem, Contribuicao, EventoDetalhe, ItemEvento, RestricaoCatalogo } from '../types/api'
import AlertaRestricao from './AlertaRestricao'
import ErroApi from './ErroApi'

export default function FormularioContribuicao({ evento }: { evento: EventoDetalhe }) {
  const { usuario } = useAuth()
  const [itens, setItens] = useState<ItemEvento[]>([])
  const [contribuicoes, setContribuicoes] = useState<Contribuicao[]>([])
  const [coberturaItens, setCoberturaItens] = useState<CoberturaItem[]>([])
  const [catalogoRestricoes, setCatalogoRestricoes] = useState<RestricaoCatalogo[]>([])
  const [itemId, setItemId] = useState<number | ''>('')
  const [quantidade, setQuantidade] = useState(1)
  const [erro, setErro] = useState<unknown>(null)
  const [enviando, setEnviando] = useState(false)

  async function carregar() {
    const [listaItens, listaContrib, cobertura, restricoes] = await Promise.all([
      listarItens(evento.id),
      listarContribuicoes(evento.id),
      buscarCobertura(evento.id),
      listarRestricoes(),
    ])
    setItens(listaItens)
    setContribuicoes(listaContrib)
    setCoberturaItens(cobertura.itens)
    setCatalogoRestricoes(restricoes)
  }

  useEffect(() => {
    void carregar().catch(setErro)
  }, [evento.id, evento.estouParticipando, evento.aceitaAlteracoes])

  const minhas = contribuicoes.filter((item) => item.usuarioId === usuario?.id)
  const itemSelecionado = itens.find((item) => item.id === itemId)
  const coberturaItem = coberturaItens.find((item) => item.itemId === itemId)
  const faltante = coberturaItem?.faltante ?? 0

  const conflitos = useMemo(() => {
    if (!itemSelecionado || !usuario) return []
    const meusAlergenos = new Set(
      catalogoRestricoes
        .filter((restricao) => usuario.restricoes.some((minha) => minha.id === restricao.id))
        .flatMap((restricao) => restricao.alergenos.map((alergeno) => alergeno.nome)),
    )
    return itemSelecionado.alergenos.map((alergeno) => alergeno.nome).filter((nome) => meusAlergenos.has(nome))
  }, [itemSelecionado, usuario, catalogoRestricoes])

  async function enviar(form: FormEvent) {
    form.preventDefault()
    if (itemId === '') return
    setErro(null)
    setEnviando(true)
    try {
      await criarContribuicao(evento.id, { itemId, quantidade })
      setQuantidade(1)
      await carregar()
    } catch (falha) {
      setErro(falha)
    } finally {
      setEnviando(false)
    }
  }

  async function remover(id: number) {
    setErro(null)
    try {
      await excluirContribuicao(evento.id, id)
      await carregar()
    } catch (falha) {
      setErro(falha)
    }
  }

  return (
    <section className="secao card">
      <h2>Contribuições</h2>
      <p>A soma nunca passa do necessário. Depois do prazo, a lista congela.</p>
      <ErroApi erro={erro} />
      {contribuicoes.length === 0 && <p className="vazio">Nenhuma contribuição registrada.</p>}
      {contribuicoes.length > 0 && (
        <table className="tabela">
          <thead>
            <tr>
              <th>Quem</th>
              <th>Item</th>
              <th>Qtd</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {contribuicoes.map((item) => (
              <tr key={item.id}>
                <td>{item.usuarioNome}</td>
                <td>{item.itemNome}</td>
                <td>{item.quantidade}</td>
                <td>
                  {item.usuarioId === usuario?.id && evento.aceitaAlteracoes && (
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

      {!evento.estouParticipando && (
        <p className="vazio">Participe do evento para registrar o que vai levar.</p>
      )}
      {!evento.aceitaAlteracoes && (
        <p className="vazio">O prazo acabou ou o evento não aceita mais alterações.</p>
      )}

      {evento.estouParticipando && evento.aceitaAlteracoes && (
        <form onSubmit={enviar} data-testid="form-contribuicao">
          <h3>O que você vai levar</h3>
          <label className="campo">
            Item
            <select value={itemId} onChange={(e) => setItemId(Number(e.target.value))} required>
              <option value="">Selecione</option>
              {itens.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.nome}
                </option>
              ))}
            </select>
          </label>
          {itemSelecionado && (
            <p>
              Restam {faltante} de {itemSelecionado.quantidadeNecessaria}.
            </p>
          )}
          <AlertaRestricao nomesAlergenos={conflitos} />
          <label className="campo">
            Quantidade
            <input
              type="number"
              min={1}
              max={Math.max(faltante, 1)}
              value={quantidade}
              onChange={(e) => setQuantidade(Number(e.target.value))}
              required
            />
          </label>
          <div className="acoes">
            <button type="submit" disabled={enviando || faltante <= 0}>
              {enviando ? 'Registrando…' : 'Registrar'}
            </button>
          </div>
          {minhas.length > 0 && <p className="ajuda">Você já registrou {minhas.length} item(ns).</p>}
        </form>
      )}
    </section>
  )
}
