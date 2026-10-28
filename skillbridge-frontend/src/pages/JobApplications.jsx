import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, MessageSquare } from 'lucide-react'
import * as applicationApi from '../api/applicationApi.js'
import * as contractApi from '../api/contractApi.js'
import * as jobApi from '../api/jobApi.js'
import * as messageApi from '../api/messageApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { formatDateTime, formatMoney } from '../utils/dates.js'
import { Badge, Button, Card, PageHeader } from '../components/ui.jsx'
import Pagination from '../components/Pagination.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { useToast } from '../components/Toast.jsx'

const NEXT = { PENDING: ['SHORTLISTED', 'REJECTED'], SHORTLISTED: ['ACCEPTED', 'REJECTED'] }

export default function JobApplications() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [busy, setBusy] = useState(null)
  const job = useFetch(() => jobApi.get(id), [id])
  const apps = useFetch(() => applicationApi.forJob(id, { page, size: 10 }), [id, page], job.data?.clientId === user.id)

  if (job.loading) return <LoadingSpinner />
  if (job.error) return <ErrorMessage error={job.error} onRetry={job.reload} />
  if (job.data.clientId !== user.id) return <ErrorMessage message="Only the client who posted this job can view its applications." />

  const act = async (key, fn, okMsg, after) => {
    setBusy(key)
    try { const r = await fn(); toast.success(okMsg); after ? after(r) : apps.reload(true) } catch (e) { toast.error(getErrorMessage(e)) } finally { setBusy(null) }
  }

  return (
    <div>
      <Link to={`/jobs/${id}`} className="mb-3 inline-flex items-center gap-1 text-sm text-muted hover:text-white"><ArrowLeft className="h-4 w-4" />Back to job</Link>
      <PageHeader title="Applications" subtitle={job.data.title} />
      {apps.loading ? <LoadingSpinner /> : apps.error ? <ErrorMessage error={apps.error} onRetry={apps.reload} /> : !apps.data.content.length ? (
        <EmptyState title="No applications yet" text="Applications will appear here as freelancers apply." />
      ) : (
        <>
          <div className="space-y-4">
            {apps.data.content.map((a) => (
              <Card key={a.id}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <Link to={`/freelancers?id=${a.applicantId}`} className="text-base font-extrabold hover:text-brand-400">{a.applicantName}</Link>
                    <p className="text-xs text-muted">{a.applicantEmail} · {formatDateTime(a.createdAt)}</p>
                  </div>
                  <Badge value={a.status} />
                </div>
                {a.proposal && (
                  <div className="mt-3 rounded-xl bg-ink-800 p-4 text-sm">
                    <p className="whitespace-pre-line text-slate-200">{a.proposal.coverLetter}</p>
                    <p className="mt-3 text-xs text-muted">Proposed <b className="text-white">{formatMoney(a.proposal.proposedPrice)}</b>{a.proposal.estimatedDeliveryDays ? ` · ${a.proposal.estimatedDeliveryDays} days` : ''} · Expected budget {formatMoney(a.expectedBudget)}</p>
                  </div>
                )}
                <div className="mt-4 flex flex-wrap gap-2">
                  {(NEXT[a.status] ?? []).map((s) => (
                    <Button key={s} size="sm" variant={s === 'REJECTED' ? 'danger' : 'primary'} loading={busy === `${a.id}${s}`} disabled={!!busy}
                      onClick={() => act(`${a.id}${s}`, () => applicationApi.setStatus(a.id, s), `Application ${s.toLowerCase()}.`)}>{s === 'SHORTLISTED' ? 'Shortlist' : s === 'ACCEPTED' ? 'Accept' : 'Reject'}</Button>
                  ))}
                  {a.status === 'ACCEPTED' && (
                    <Button size="sm" loading={busy === `c${a.id}`} disabled={!!busy}
                      onClick={() => act(`c${a.id}`, () => contractApi.create(a.id), 'Contract created.', (c) => navigate(`/contracts/${c.id}`))}>Create contract</Button>
                  )}
                  <Button size="sm" variant="secondary" disabled={!!busy}
                    onClick={() => act(`m${a.id}`, () => messageApi.startConversation(a.applicantId), 'Opening conversation…', (c) => navigate(`/messages/${c.id}`))}><MessageSquare className="h-3.5 w-3.5" />Message</Button>
                </div>
              </Card>
            ))}
          </div>
          <Pagination page={apps.data.number} totalPages={apps.data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
