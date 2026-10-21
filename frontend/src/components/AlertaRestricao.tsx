export default function AlertaRestricao({ nomesAlergenos }: { nomesAlergenos: string[] }) {
  if (nomesAlergenos.length === 0) {
    return null
  }
  return (
    <div className="alerta alerta-aviso" role="status">
      Este item contém {nomesAlergenos.join(', ')}, em conflito com a sua restrição alimentar.
    </div>
  )
}
