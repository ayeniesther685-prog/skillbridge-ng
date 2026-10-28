import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Calendar, MapPin, MessageSquare, Pencil, Trash2, Users } from 'lucide-react'
import * as jobApi from '../api/jobApi.js'
import * as applicationApi from '../api/applicationApi.js'
import * as messageApi from '../api/messageApi.js'
import * as reviewApi from '../api/reviewApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { budgetRange, formatDate, formatDateTime, label } from '../utils/dates.js'
import { Badge, Button, Card, Field, inputCls, textareaCls } from '../components/ui.jsx'
import SkillPill from '../components/SkillPill.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useToast } from '../components/Toast.jsx'

function ApplyForm({ job, onDone }) {
  const toast = useToast()
  const [f, setF] = useState({ expectedBudget: '', coverLetter: '', proposedPrice: '', estimatedDeliveryDays: '' })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    if (saving) return
    const price = Number(f.proposedPrice)
    if (!f.coverLetter.trim()) return setError('Cover letter is required.')
    if (!(price > 0)) return setError('Proposed price must be greater than 0.')
    setSaving(true); setError('')
    try {
      await applicationApi.apply(job.id, {
        expectedBudget: f.expectedBudget === '' ? price : Number(f.expectedBudget),
        proposal: { coverLetter: f.coverLetter.trim(), proposedPrice: price, estimatedDeliveryDays: f.estimatedDeliveryDays === '' ? null : Number(f.estimatedDeliveryDays) },
      })
      toast.success('Application submitted.')
      onDone()
    } catch (err) { setError(getErrorMessage(err)) } finally { setSaving(false) }
  }
  return (
    <Card>
      <h2 className="mb-4 text-lg font-extrabold">Apply for this job</h2>
      <form onSubmit={submit} noValidate className="space-y-4">
        {error && <ErrorMessage message={error} />}
        <Field label="Cover letter *" hint={`${f.coverLetter.length}/4000`}><textarea className={textareaCls} maxLength={4000} value={f.coverLetter} onChange={set('coverLetter')} /></Field>
        <div className="grid gap-4 sm:grid-cols-3">
          <Field label="Proposed price (₦) *"><input className={inputCls} type="number" min="1" step="0.01" value={f.proposedPrice} onChange={set('proposedPrice')} /></Field>
          <Field label="Expected budget (₦)" hint="Defaults to proposed price"><input className={inputCls} type="number" min="0" step="0.01" value={f.expectedBudget} onChange={set('expectedBudget')} /></Field>
          <Field label="Delivery (days)"><input className={inputCls} type="number" min="1" value={f.estimatedDeliveryDays} onChange={set('estimatedDeliveryDays')} /></Field>
        </div>
        <Button type="submit" loading={saving}>{saving ? 'Submitting…' : 'Submit application'}</Button>
      </form>
    </Card>
  )
}

export default function JobDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const toast = useToast()
  const { user, isAuthenticated } = useAuth()
  const { data: job, loading, error, reload } = useFetch(() => jobApi.get(id), [id])
  const { data: reviews } = useFetch(() => reviewApi.forJob(id, { size: 10 }), [id], isAuthenticated)
  const [busy, setBusy] = useState(false)
  const [applied, setApplied] = useState(false)

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorMessage error={error} onRetry={reload} />
  const isOwner = user?.id === job.clientId

  const remove = async () => {
    if (!window.confirm('Delete this job? This cannot be undone.')) return
    setBusy(true)
    try { await jobApi.remove(job.id); toast.success('Job deleted.'); navigate('/jobs') } catch (e) { toast.error(getErrorMessage(e)); setBusy(false) }
  }
  const changeStatus = async (status) => {
    setBusy(true)
    try { await jobApi.setStatus(job.id, status); toast.success(`Job marked ${label(status).toLowerCase()}.`); reload(true) } catch (e) { toast.error(getErrorMessage(e)) } finally { setBusy(false) }
  }
  const messageClient = async () => {
    setBusy(true)
    try { const c = await messageApi.startConversation(job.clientId); navigate(`/messages/${c.id}`) } catch (e) { toast.error(getErrorMessage(e)); setBusy(false) }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_20rem]">
      <div className="space-y-6">
        <Card>
          <div className="flex flex-wrap items-start justify-between gap-3">
            <h1 className="text-2xl font-extrabold sm:text-3xl">{job.title}</h1>
            <Badge value={job.status} />
          </div>
          <p className="mt-2 text-sm text-muted">Posted by <Link className="font-semibold text-brand-400" to={`/freelancers?id=${job.clientId}`}>{job.clientName}</Link> · {formatDateTime(job.createdAt)}</p>
          <div className="mt-4 flex flex-wrap gap-x-5 gap-y-2 text-sm text-slate-300">
            <span className="font-bold text-white">{budgetRange(job.budgetMin, job.budgetMax)}</span>
            {job.location && <span className="inline-flex items-center gap-1"><MapPin className="h-4 w-4" />{job.location}</span>}
            <span>{label(job.jobType)} · {label(job.experienceLevel)}</span>
            {job.deadline && <span className="inline-flex items-center gap-1"><Calendar className="h-4 w-4" />Due {formatDate(job.deadline)}</span>}
          </div>
          <p className="mt-5 whitespace-pre-line text-sm leading-relaxed text-slate-200">{job.description}</p>
          {job.requiredSkills?.length > 0 && <div className="mt-5 flex flex-wrap gap-2">{job.requiredSkills.map((s) => <SkillPill key={s.id} skill={s} />)}</div>}
        </Card>

        {isAuthenticated && !isOwner && job.status === 'OPEN' && (applied
          ? <Card><p className="font-bold text-emerald-300">Application submitted.</p><Link to="/applications" className="text-sm text-brand-400">View my applications →</Link></Card>
          : <ApplyForm job={job} onDone={() => setApplied(true)} />)}
        {!isAuthenticated && <Card className="text-center"><p className="font-bold">Sign in to apply or message the client.</p><Link to="/login" state={{ from: { pathname: `/jobs/${job.id}` } }} className="mt-3 inline-flex h-11 items-center rounded-xl bg-brand-500 px-5 text-sm font-bold">Sign in</Link></Card>}

        {reviews?.content?.length > 0 && (
          <Card><h2 className="mb-3 text-lg font-extrabold">Reviews for this job</h2>
            <ul className="space-y-3">{reviews.content.map((r) => <li key={r.id}><p className="text-sm font-bold">{'★'.repeat(r.rating)} <span className="font-normal text-muted">{r.reviewerName} → {r.revieweeName}</span></p>{r.comment && <p className="text-sm text-slate-300">{r.comment}</p>}</li>)}</ul>
          </Card>
        )}
      </div>

      <aside className="space-y-3">
        {isOwner ? (
          <Card className="space-y-2">
            <h2 className="font-extrabold">Manage job</h2>
            <Link to={`/jobs/${job.id}/applications`} className="flex h-11 items-center justify-center gap-2 rounded-xl bg-brand-500 text-sm font-bold hover:bg-brand-600"><Users className="h-4 w-4" />View applications</Link>
            <Link to={`/jobs/${job.id}/edit`} className="flex h-11 items-center justify-center gap-2 rounded-xl border border-line bg-ink-800 text-sm font-bold"><Pencil className="h-4 w-4" />Edit job</Link>
            {job.status === 'OPEN' && <Button variant="secondary" className="w-full" disabled={busy} onClick={() => changeStatus('CLOSED')}>Close job</Button>}
            {job.status === 'CLOSED' && <Button variant="secondary" className="w-full" disabled={busy} onClick={() => changeStatus('OPEN')}>Reopen job</Button>}
            <Button variant="danger" className="w-full" disabled={busy} onClick={remove}><Trash2 className="h-4 w-4" />Delete job</Button>
          </Card>
        ) : isAuthenticated && (
          <Card><Button variant="secondary" className="w-full" loading={busy} onClick={messageClient}><MessageSquare className="h-4 w-4" />Message client</Button></Card>
        )}
      </aside>
    </div>
  )
}
