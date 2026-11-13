import { walletApi } from '../../api/wallet'
import { usePaged } from '../../hooks/useApi'
import QueryBoundary from '../../components/ui/QueryBoundary'
import Pagination from '../../components/ui/Pagination'
import DataTable from '../../components/ui/DataTable'
import EmptyState from '../../components/ui/EmptyState'
import StatusBadge from '../../components/ui/StatusBadge'
import { fmtDate } from '../../utils/format'
export default function Withdrawals() {
  const { q, setPage } = usePaged(['wallet', 'withdrawals'], walletApi.withdrawals)
  return <QueryBoundary q={q}>{(d) => d.content.length === 0 ? <EmptyState title="No withdrawal requests" description="Requests you make will be listed with their status." /> : (
    <><DataTable rows={d.content} columns={[
      { key: 'createdAt', header: 'Requested', render: (r) => fmtDate(r.createdAt) },
      { key: 'amount', header: 'Points', render: (r) => r.amountPoints ?? r.amount },
      { key: 'bank', header: 'Bank', render: (r) => r.bankName },
      { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.status} /> },
    ]} /><Pagination page={d} onPage={setPage} /></>)}</QueryBoundary>
}
