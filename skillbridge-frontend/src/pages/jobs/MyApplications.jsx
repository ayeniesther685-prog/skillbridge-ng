import { Link } from 'react-router-dom'
import { FileText } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import DataTable from '../../components/ui/DataTable'
import StatusBadge from '../../components/ui/StatusBadge'
import Pagination from '../../components/ui/Pagination'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import Button from '../../components/ui/Button'
import { applicationsApi } from '../../api/applications'
import { usePaged, useSave } from '../../hooks/useApi'
import { fmtDate } from '../../utils/format'
export default function MyApplications() {
  const { q, setPage } = usePaged(['applications', 'mine'], applicationsApi.list)
  const withdraw = useSave(applicationsApi.withdraw, { invalidate: [['applications']], success: 'Application withdrawn' })
  const cols = [
    { key: 'job', header: 'Job', render: (a) => { const jid = a.jobId ?? a.job?.id; return jid ? <Link className="text-accent hover:underline" to={`/jobs/${jid}`}>{a.jobTitle || a.job?.title || `Job #${jid}`}</Link> : '—' } },
    { key: 'createdAt', header: 'Applied', render: (a) => fmtDate(a.createdAt || a.appliedAt) },
    { key: 'status', header: 'Status', render: (a) => <StatusBadge status={a.status} /> },
    { key: 'act', header: '', render: (a) => a.status === 'PENDING' && <Button size="sm" variant="outline" loading={withdraw.isPending && withdraw.variables === a.id} onClick={() => window.confirm('Withdraw this application?') && withdraw.mutate(a.id)}>Withdraw</Button> },
  ]
  return (
    <div>
      <PageHeader title="My applications" description="Track every job you've applied to." />
      <QueryBoundary q={q}>{(d) => d.content.length === 0
        ? <EmptyState icon={FileText} title="You haven't applied to any jobs" description="Find a job that fits your skills and send a proposal." action={<Button as={Link} to="/jobs" size="sm">Browse jobs</Button>} />
        : <><DataTable columns={cols} rows={d.content} /><Pagination page={d} onPage={setPage} /></>}</QueryBoundary>
    </div>
  )
}
