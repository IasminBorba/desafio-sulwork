const fuso = 'America/Recife'

export function formatarData(iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: fuso,
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(iso))
}

export function instantParaLocal(iso: string): string {
  const data = new Date(iso)
  const partes = new Intl.DateTimeFormat('sv-SE', {
    timeZone: fuso,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).formatToParts(data)
  const valor = (tipo: Intl.DateTimeFormatPartTypes) => partes.find((parte) => parte.type === tipo)?.value ?? ''
  return `${valor('year')}-${valor('month')}-${valor('day')}T${valor('hour')}:${valor('minute')}`
}

export function localParaInstant(valor: string): string {
  return new Date(valor).toISOString()
}
