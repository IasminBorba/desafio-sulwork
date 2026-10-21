import type { RestricaoCatalogo } from '../types/api'

export default function SelecaoRestricoes({
  restricoes,
  selecionadas,
  onChange,
}: {
  restricoes: RestricaoCatalogo[]
  selecionadas: number[]
  onChange: (ids: number[]) => void
}) {
  function alternar(id: number) {
    onChange(selecionadas.includes(id) ? selecionadas.filter((atual) => atual !== id) : [...selecionadas, id])
  }

  return (
    <div className="lista-check">
      {restricoes.map((restricao) => (
        <label key={restricao.id}>
          <input
            type="checkbox"
            checked={selecionadas.includes(restricao.id)}
            onChange={() => alternar(restricao.id)}
          />
          {restricao.nome}
        </label>
      ))}
    </div>
  )
}
