import { ApiError } from '../api/client'

export default function ErroApi({ erro }: { erro: unknown }) {
  if (!erro) {
    return null
  }
  if (erro instanceof ApiError) {
    return (
      <div className="alerta alerta-erro" role="alert">
        <strong>{erro.title}</strong>
        <p>{erro.detail}</p>
        {erro.erros.length > 0 && (
          <ul>
            {erro.erros.map((campo) => (
              <li key={`${campo.campo}-${campo.mensagem}`}>
                {campo.campo}: {campo.mensagem}
              </li>
            ))}
          </ul>
        )}
      </div>
    )
  }
  return (
    <div className="alerta alerta-erro" role="alert">
      Não foi possível falar com o servidor. Tente de novo em instantes.
    </div>
  )
}
