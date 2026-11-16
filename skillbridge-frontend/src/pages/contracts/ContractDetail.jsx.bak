import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import { Plus, Star } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'
import Button from '../../components/ui/Button'
import Dialog from '../../components/ui/Dialog'
import AutoForm from '../../components/ui/AutoForm'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { contractsApi } from '../../api/contracts'
import { reviewsApi } from '../../api/reviews'
import { useAuth } from '../../context/AuthContext'
import { useSave } from '../../hooks/useApi'
import { idOf, nameOf } from '../../utils/ids'
import { naira, fmtDate } from '../../utils/format'
import { num, optNum, optStr, reqStr } from '../../utils/validators'

const msSchema = z.object({ title: reqStr('Enter a title', 200), description: optStr(), amount: optNum(), dueDate: optStr() })
const msFields = [{ name: 'title', label: 'Title', wide: true }, { name: 'description', label: 'Description', type: 'textarea' }, { name: 'amount', label: 'Amount (₦)', type: 'number' }, { name: 'dueDate', label: 'Due date', type: 'date' }]
const reviewSchema = z.object({ rating: num('Pick a rating from 1 to 5').pipe(z.number().min(1).max(5)), comment: optStr() })

export default function ContractDetail() {
  const { id } = useParams()
  const { user, hasRole } = useAuth()
  const [msDialog, setMsDialog] = useState(null)
  const [reviewOpen, setReviewOpen] = useState(false)
  const c = useQuery({ queryKey: ['contract', id], queryFn: () => contractsApi.get(id) })
  const ms = useQuery({ queryKey: ['milestones', id], queryFn: () => contractsApi.milestones(id) })
  const inv = [['contract', id], ['milestones', id], ['contracts']]
  const setStatus = useSave(contractsApi.setStatus, { invalidate: inv, success: 'Contract updated' })
  const addMs = useSave(contractsApi.addMilestone, { invalidate: inv, success: 'Milestone added', onSuccess: () => setMsDialog(null) })
  const editMs = useSave(contractsApi.updateMilestone, { invalidate: inv, success: 'Milestone saved', onSuccess: () => setMsDialog(null) })
  const msStatus = useSave(contractsApi.setMilestoneStatus, { invalidate: inv, success: 'Milestone updated' })
  const rmMs = useSave(contractsApi.removeMilestone, { invalidate: inv, success: 'Milestone removed' })
  const review = useSave(reviewsApi.create, { success: 'Review sent', onSuccess: () => setReviewOpen(false) })
  const list = Array.isArray(ms.data) ? ms.data : ms.data?.content || []

  return (
    <QueryBoundary q={c}>{(k) => {
      const clientId = idOf(k, 'client')
      const isClient = clientId != null ? clientId === user.id : hasRole('CLIENT')
      const other = isClient ? k.freelancer : k.client
      const otherId = (isClient ? idOf(k, 'freelancer') : clientId)
      const active = k.status === 'ACTIVE'
      return (
        <div>
          <Breadcrumbs items={[['Contracts', '/contracts'], [`Contract #${id}`]]} />
          <PageHeader title={k.jobTitle || k.job?.title || k.title || `Contract #${id}`} description={`${isClient ? 'Freelancer' : 'Client'}: ${nameOf(other) || '—'}`}
            actions={<>
              {isClient && active && <><Button size="sm" onClick={() => window.confirm('Mark this contract as completed?') && setStatus.mutate({ id, status: 'COMPLETED' })} disabled={setStatus.isPending}>Mark completed</Button>
                <Button size="sm" variant="outline" onClick={() => window.confirm('Cancel this contract?') && setStatus.mutate({ id, status: 'CANCELLED' })} disabled={setStatus.isPending}>Cancel</Button></>}
              {k.status === 'COMPLETED' && otherId && <Button size="sm" variant="outline" onClick={() => setReviewOpen(true)}><Star className="h-4 w-4" />Leave a review</Button>}
            </>} />
          <Card className="mb-6"><CardBody className="grid gap-3 text-sm sm:grid-cols-4">
            <div><p className="text-muted">Status</p><StatusBadge status={k.status} /></div>
            <div><p className="text-muted">Amount</p>{naira(k.totalAmount ?? k.amount)}</div>
            <div><p className="text-muted">Start</p>{fmtDate(k.startDate || k.createdAt)}</div>
            <div><p className="text-muted">End</p>{fmtDate(k.endDate)}</div>
            {k.terms && <div className="sm:col-span-4"><p className="text-muted">Terms</p><p className="whitespace-pre-line">{k.terms}</p></div>}
          </CardBody></Card>

          <div className="mb-3 flex items-center justify-between"><h2 className="font-semibold">Milestones</h2>
            {isClient && active && <Button size="sm" onClick={() => setMsDialog({})}><Plus className="h-4 w-4" />Add milestone</Button>}</div>
          <QueryBoundary q={ms} rows={2}>{() => list.length === 0
            ? <EmptyState title="No milestones yet" description={isClient ? 'Break the work into milestones so progress is clear.' : 'Your client hasn’t added milestones yet.'} />
            : <div className="space-y-3">{list.map((m) => (
              <Card key={m.id}><CardBody className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0 text-sm"><p className="font-medium">{m.title}</p>{m.description && <p className="text-muted">{m.description}</p>}
                  <p className="mt-1 text-xs text-muted">{naira(m.amount)}{m.dueDate ? ` · due ${fmtDate(m.dueDate)}` : ''}</p></div>
                <div className="flex flex-wrap items-center gap-2"><StatusBadge status={m.status} />
                  {!isClient && m.status === 'PENDING' && <Button size="sm" onClick={() => msStatus.mutate({ id: m.id, status: 'IN_PROGRESS' })} disabled={msStatus.isPending}>Start work</Button>}
                  {!isClient && m.status === 'IN_PROGRESS' && <Button size="sm" onClick={() => msStatus.mutate({ id: m.id, status: 'SUBMITTED' })} disabled={msStatus.isPending}>Submit for review</Button>}
                  {isClient && m.status === 'SUBMITTED' && <><Button size="sm" onClick={() => msStatus.mutate({ id: m.id, status: 'APPROVED' })} disabled={msStatus.isPending}>Approve</Button>
                    <Button size="sm" variant="outline" onClick={() => msStatus.mutate({ id: m.id, status: 'REJECTED' })} disabled={msStatus.isPending}>Request changes</Button></>}
                  {isClient && m.status === 'PENDING' && <><Button size="sm" variant="ghost" onClick={() => setMsDialog(m)}>Edit</Button>
                    <Button size="sm" variant="ghost" onClick={() => window.confirm('Delete this milestone?') && rmMs.mutate(m.id)}>Delete</Button></>}
                </div>
              </CardBody></Card>))}</div>}</QueryBoundary>

          <Dialog open={!!msDialog} onClose={() => setMsDialog(null)} title={msDialog?.id ? 'Edit milestone' : 'Add milestone'}>
            {msDialog && <AutoForm key={msDialog.id ?? 'new'} fields={msFields} schema={msSchema} defaultValues={Object.fromEntries(msFields.map((f) => [f.name, msDialog[f.name] ?? '']))}
              onSubmit={(v) => (msDialog.id ? editMs.mutateAsync({ id: msDialog.id, ...v }) : addMs.mutateAsync({ contractId: id, ...v }))} onCancel={() => setMsDialog(null)} />}
          </Dialog>
          <Dialog open={reviewOpen} onClose={() => setReviewOpen(false)} title="Leave a review">
            <AutoForm schema={reviewSchema} defaultValues={{ rating: '', comment: '' }} fields={[{ name: 'rating', label: 'Rating (1–5)', type: 'number' }, { name: 'comment', label: 'Comment', type: 'textarea' }]}
              onSubmit={(v) => review.mutateAsync({ jobId: k.jobId ?? k.job?.id, revieweeId: otherId, ...v })} submitLabel="Send review" onCancel={() => setReviewOpen(false)} />
          </Dialog>
        </div>)
    }}</QueryBoundary>
  )
}
