import { useNavigate } from 'react-router-dom'
import { FileSignature } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import DataTable from '../../components/ui/DataTable'
import StatusBadge from '../../components/ui/StatusBadge'
import Pagination from '../../components/ui/Pagination'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { contractsApi } from '../../api/contracts'
import { usePaged } from '../../hooks/useApi'
import { naira, fmtDate } from '../../utils/format'
import { nameOf } from '../../utils/ids'
export default function Contracts() {
  const nav = useNavigate()
  const { q, setPage } = usePaged(['contracts', 'list'], contractsApi.list)
  const cols = [
    { key: 'title', header: 'Contract', render: (c) => c.jobTitle || c.job?.title || c.title || `Contract #${c.id}` },
    { key: 'party', header: 'With', render: (c) => nameOf(c.freelancer || c.client) || '—' },
    { key: 'amount', header: 'Amount', render: (c) => naira(c.totalAmount ?? c.amount) },
    { key: 'createdAt', header: 'Created', render: (c) => fmtDate(c.createdAt || c.startDate) },
    { key: 'status', header: 'Status', render: (c) => <StatusBadge status={c.status} /> },
  ]
  return (
    <div>
      <PageHeader title="Contracts" description="Agreements created from accepted applications." />
      <QueryBoundary q={q}>{(d) => d.content.length === 0
        ? <EmptyState icon={FileSignature} title="No contracts yet" description="A client creates a contract after accepting an application." />
        : <><DataTable columns={cols} rows={d.content} onRowClick={(c) => nav(`/contracts/${c.id}`)} /><Pagination page={d} onPage={setPage} /></>}</QueryBoundary>
    </div>
  )
}
