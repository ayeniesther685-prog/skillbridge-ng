import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { MessageSquare, Pencil, Plus, Trash2 } from 'lucide-react'
import * as contractApi from '../api/contractApi.js'
import * as milestoneApi from '../api/milestoneApi.js'
import * as reviewApi from '../api/reviewApi.js'
import * as messageApi from '../api/messageApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { formatDate, formatMoney } from '../utils/dates.js'
import { Badge, Button, Card, Field, Modal, inputCls, textareaCls } from '../components/ui.jsx'
import ComingSoon from '../components/ComingSoon.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useToast } from '../components/Toast.jsx'

const TERMINAL = ['COMPLETED', 'CANCELLED']

function MilestoneModal({ contractId, milestone, onClose, onSaved }) {
  const toast = useToast()
  const [f, setF] = useState({ title: milestone?.title ?? '', description: milestone?.description ?? '', amount: milestone?.amount ?? '', dueDate: milestone?.dueDate ?? '' })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }))
  const submit = async (e) => {
    e.preventDefault()
    if (saving) return
    if (!f.title.trim()) return setError('Title is required.')
    if (!(Number(f.amount) > 0)) return setError('Amount must be greater than 0.')
    const body = { title: f.title.trim(), description: f.description.trim() || null, amount: Number(f.amount), dueDate: f.dueDate || null }
    setSaving(true); setError('')
    try {
      if (milestone) await milestoneApi.update(milestone.id, body); else await milestoneApi.create(contractId, body)
      toast.success(milestone ? 'Milestone updated.' : 'Milestone added.')
      onSaved()
    } catch (err) { setError(getErrorMessage(err)); setSaving(false) }
  }
  return (
    <Modal open title={milestone ? 'Edit milestone' : 'Add milestone'} onClose={() => !saving && onClose()}>
      <form onSubmit={submit} noValidate className="space-y-4">
        {error && <ErrorMessage message={error} />}
        <Field label="Title *"><input className={inputCls} maxLength={200} value={f.title} onChange={set('title')} /></Field>
        <Field label="Description"><textarea className={textareaCls} maxLength={2000} value={f.description} onChange={set('description')} /></Field>
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Amount (₦) *"><input className={inputCls} type="number" min="0.01" step="0.01" value={f.amount} onChange={set('amount')} /></Field>
          <Field label="Due date"><input className={inputCls} type="date" value={f.dueDate} onChange={set('dueDate')} /></Field>
        </div>
        <div className="flex justify-end gap-2"><Button type="button" variant="secondary" onClick={onClose} disabled={saving}>Cancel</Button><Button type="submit" loading={saving}>Save</Button></div>
      </form>
    </Modal>
  )
}

function ReviewBox({ contract, revieweeId, revieweeName, me }) {
  const toast = useToast()
  const { data, reload } = useFetch(() => reviewApi.forJob(contract.jobId, { size: 50 }), [contract.jobId])
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [saving, setSaving] = useState(false)
  const already = data?.content?.some((r) => r.reviewerId === me)
  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try { await reviewApi.create({ jobId: contract.jobId, revieweeId, rating, comment: comment.trim() || null }); toast.success('Review submitted.'); reload(true) }
    catch (err) { toast.error(getErrorMessage(err)) } finally { setSaving(false) }
  }
  return (
    <Card>
      <h2 className="mb-3 text-lg font-extrabold">Reviews</h2>
      {data?.content?.map((r) => <p key={r.id} className="mb-2 text-sm"><b>{'★'.repeat(r.rating)}</b> <span className="text-muted">{r.reviewerName} → {r.revieweeName}</span>{r.comment && <span className="block text-slate-300">{r.comment}</span>}</p>)}
      {contract.status === 'COMPLETED' && !already && (
        <form onSubmit={submit} className="mt-3 space-y-3 border-t border-line pt-4">
          <p className="text-sm font-semibold">Review {revieweeName}</p>
          <div className="flex gap-1" role="radiogroup" aria-label="Rating">
            {[1, 2, 3, 4, 5].map((n) => <button type="button" key={n} role="radio" aria-checked={rating === n} aria-label={`${n} star${n > 1 ? 's' : ''}`} onClick={() => setRating(n)} className={`text-2xl ${n <= rating ? 'text-amber-300' : 'text-slate-600'}`}>★</button>)}
          </div>
          <textarea className={textareaCls} maxLength={2000} value={comment} onChange={(e) => setComment(e.target.value)} placeholder="Share your experience (optional)" aria-label="Review comment" />
          <Button type="submit" loading={saving}>Submit review</Button>
        </form>
      )}
      {contract.status === 'COMPLETED' && already && <p className="text-sm text-muted">You've reviewed this job.</p>}
      {contract.status !== 'COMPLETED' && !data?.content?.length && <p className="text-sm text-muted">Reviews open once the contract is completed.</p>}
    </Card>
  )
}

export default function ContractDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const { data: c, loading, error, reload } = useFetch(() => contractApi.get(id), [id])
  const [modal, setModal] = useState(null) // null | 'new' | milestone
  const [busy, setBusy] = useState(null)

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorMessage error={error} onRetry={reload} />
  const isClient = c.clientId === user.id
  const otherId = isClient ? c.freelancerId : c.clientId
  const otherName = isClient ? c.freelancerName : c.clientName

  const run = async (key, fn, msg) => {
    setBusy(key)
    try { await fn(); toast.success(msg); reload(true) } catch (e) { toast.error(getErrorMessage(e)) } finally { setBusy(null) }
  }
  const message = async () => {
    try { const conv = await messageApi.startConversation(otherId); navigate(`/messages/${conv.id}`) } catch (e) { toast.error(getErrorMessage(e)) }
  }
  const total = c.milestones?.reduce((n, m) => n + Number(m.amount), 0) ?? 0

  return (
    <div className="space-y-6">
      <Card>
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="text-2xl font-extrabold">{c.title || c.jobTitle}</h1>
            <p className="mt-1 text-sm text-muted">Job: <Link className="text-brand-400" to={`/jobs/${c.jobId}`}>{c.jobTitle}</Link> · Client: {c.clientName} · Freelancer: {c.freelancerName}</p>
          </div>
          <Badge value={c.status} />
        </div>
        {c.description && <p className="mt-3 whitespace-pre-line text-sm text-slate-300">{c.description}</p>}
        <div className="mt-4 flex flex-wrap items-center gap-x-6 gap-y-2 text-sm">
          <span>Amount: <b className="text-lg">{formatMoney(c.amount)}</b></span>
          <span className="text-muted">Start {formatDate(c.startDate)} · End {formatDate(c.endDate)}</span>
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          <Button variant="secondary" size="sm" onClick={message}><MessageSquare className="h-3.5 w-3.5" />Message {otherName}</Button>
          {isClient && !TERMINAL.includes(c.status) && (
            <Button variant="danger" size="sm" loading={busy === 'cancel'} disabled={!!busy} onClick={() => window.confirm('Cancel this contract?') && run('cancel', () => contractApi.setStatus(c.id, 'CANCELLED'), 'Contract cancelled.')}>Cancel contract</Button>
          )}
        </div>
      </Card>

      <Card>
        <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-lg font-extrabold">Milestones <span className="text-sm font-normal text-muted">({formatMoney(total)} of {formatMoney(c.amount)} planned)</span></h2>
          {isClient && !TERMINAL.includes(c.status) && <Button size="sm" onClick={() => setModal('new')}><Plus className="h-4 w-4" />Add milestone</Button>}
        </div>
        {!c.milestones?.length ? <p className="text-sm text-muted">No milestones yet.{isClient ? ' Add one to break the work into stages.' : ''}</p> : (
          <ul className="space-y-3">
            {c.milestones.map((m) => (
              <li key={m.id} className="rounded-xl border border-line bg-ink-800 p-4">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div><p className="font-bold">{m.title}</p>{m.description && <p className="text-sm text-slate-300">{m.description}</p>}
                    <p className="mt-1 text-xs text-muted">{formatMoney(m.amount)}{m.dueDate ? ` · Due ${formatDate(m.dueDate)}` : ''}{m.completedAt ? ` · Completed ${formatDate(m.completedAt)}` : ''}</p></div>
                  <Badge value={m.status} />
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  {!isClient && m.status === 'PENDING' && <Button size="sm" loading={busy === `s${m.id}`} disabled={!!busy} onClick={() => run(`s${m.id}`, () => milestoneApi.setStatus(m.id, 'IN_PROGRESS'), 'Milestone started.')}>Start work</Button>}
                  {!isClient && m.status === 'IN_PROGRESS' && <Button size="sm" loading={busy === `s${m.id}`} disabled={!!busy} onClick={() => run(`s${m.id}`, () => milestoneApi.setStatus(m.id, 'COMPLETED'), 'Milestone marked complete.')}>Mark complete</Button>}
                  {isClient && m.status === 'COMPLETED' && <Button size="sm" loading={busy === `s${m.id}`} disabled={!!busy} onClick={() => run(`s${m.id}`, () => milestoneApi.setStatus(m.id, 'APPROVED'), 'Milestone approved.')}>Approve</Button>}
                  {isClient && m.status === 'PENDING' && (<>
                    <Button size="sm" variant="secondary" onClick={() => setModal(m)}><Pencil className="h-3.5 w-3.5" />Edit</Button>
                    <Button size="sm" variant="danger" loading={busy === `d${m.id}`} disabled={!!busy} onClick={() => window.confirm('Delete this milestone?') && run(`d${m.id}`, () => milestoneApi.remove(m.id), 'Milestone deleted.')}><Trash2 className="h-3.5 w-3.5" />Delete</Button>
                  </>)}
                </div>
              </li>
            ))}
          </ul>
        )}
        <p className="mt-3 text-xs text-muted">The contract completes automatically once every milestone is approved.</p>
      </Card>

      <ReviewBox contract={c} revieweeId={otherId} revieweeName={otherName} me={user.id} />
      <div className="max-w-sm"><ComingSoon title="Payments" phase="Phase 6" description="Fund and release milestone payments securely." buttonLabel="Fund milestone" /></div>

      {modal && <MilestoneModal contractId={c.id} milestone={modal === 'new' ? null : modal} onClose={() => setModal(null)} onSaved={() => { setModal(null); reload(true) }} />}
    </div>
  )
}
