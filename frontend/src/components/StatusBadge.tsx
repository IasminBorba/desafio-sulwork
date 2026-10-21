import type { StatusEvento } from '../types/api'
import { LABEL_STATUS } from '../utils/labels'

export default function StatusBadge({ status }: { status: StatusEvento }) {
  return <span className={`badge ${status}`}>{LABEL_STATUS[status]}</span>
}
