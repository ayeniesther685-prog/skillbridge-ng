import Badge from './Badge'
import { label } from '../../utils/format'
const map = {
  OPEN: 'success', ACTIVE: 'success', ACCEPTED: 'success', COMPLETED: 'success', APPROVED: 'success', PAID: 'success',
  PENDING: 'warning', IN_PROGRESS: 'info', SUBMITTED: 'info', REVIEWED: 'info', DRAFT: 'neutral',
  REJECTED: 'danger', CANCELLED: 'danger', CLOSED: 'neutral', WITHDRAWN: 'neutral', DISPUTED: 'danger',
}
export default function StatusBadge({ status }) {
  return <Badge variant={map[status] || 'neutral'}>{label(status)}</Badge>
}
