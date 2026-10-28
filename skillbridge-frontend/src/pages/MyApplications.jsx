import { useState } from 'react'
import { Link } from 'react-router-dom'
import { FileText } from 'lucide-react'
import * as applicationApi from '../api/applicationApi.js'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { formatDateTime, formatMoney } from '../utils/dates.js'
import { Badge, Button, Card, PageHeader } from '../components/ui.jsx'
import Pagination from '../components/Pagination.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { useToast } from '../components/Toast.jsx'

export default function MyApplications() {
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [busy, setBusy] = useState(null)
  const { data, loading, error, reload } = useFetch(() => applicationApi.mine({ page, size: 10 }), [page])

  const act = async (key, fn, msg) => {
    setBusy(key)
    try { await fn(); toast.success(msg); reload(true) } catch (e) { toast.error(getErrorMessage(e)) } finally { setBusy(null) }
  }

  return (
    <div>
      <PageHeader title="My applications" subtitle="Jobs you've applied to." />
      {loading ? <LoadingSpinner /> : error ? <ErrorMessage error={error} onRetry={reload} /> : !data.content.length ? (
        <EmptyState icon={FileText} title="No applications yet" text="Find a job and send your first proposal." action={<Link to="/jobs" className="inline-flex h-11 items-center rounded-xl bg-brand-500 px-5 text-sm font-bold">Browse jobs</Link>} />
      ) : (
        <>
          <div className="space-y-4">
            {data.content.map((a) => (
              <Card key={a.id}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div><Link to={`/jobs/${a.jobId}`} className="text-base font-extrabold hover:text-brand-400">{a.jobTitle}</Link><p className="text-xs text-muted">Applied {formatDateTime(a.createdAt)}</p></div>
                  <Badge value={a.status} />
                </div>
                {a.proposal && <p className="mt-3 line-clamp-3 text-sm text-slate-300">{a.proposal.coverLetter}</p>}
                <p className="mt-2 text-xs text-muted">Proposed <b className="text-white">{formatMoney(a.proposal?.proposedPrice)}</b>{a.proposal?.estimatedDeliveryDays ? ` · ${a.proposal.estimatedDeliveryDays} days` : ''}</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {['PENDING', 'SHORTLISTED'].includes(a.status) && (
                    <Button size="sm" variant="secondary" loading={busy === `w${a.id}`} disabled={!!busy} onClick={() => window.confirm('Withdraw this application?') && act(`w${a.id}`, () => applicationApi.withdraw(a.id), 'Application withdrawn.')}>Withdraw</Button>
                  )}
                  <Button size="sm" variant="danger" loading={busy === `d${a.id}`} disabled={!!busy} onClick={() => window.confirm('Delete this application?') && act(`d${a.id}`, () => applicationApi.remove(a.id), 'Application deleted.')}>Delete</Button>
                </div>
              </Card>
            ))}
          </div>
          <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
