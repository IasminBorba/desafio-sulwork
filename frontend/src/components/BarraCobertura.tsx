export default function BarraCobertura({
  percentual,
  rotulo,
}: {
  percentual: number
  rotulo?: string
}) {
  const limitado = Math.max(0, Math.min(100, percentual))
  const faixa = limitado < 50 ? 'baixa' : limitado < 80 ? 'media' : ''
  return (
    <div>
      {rotulo && (
        <div className="meta-linha" style={{ marginBottom: '0.35rem' }}>
          <strong>{rotulo}</strong>
          <span>{limitado}%</span>
        </div>
      )}
      <div className={`barra ${faixa}`} role="progressbar" aria-valuenow={limitado} aria-valuemin={0} aria-valuemax={100}>
        <span style={{ width: `${limitado}%` }} />
      </div>
    </div>
  )
}
