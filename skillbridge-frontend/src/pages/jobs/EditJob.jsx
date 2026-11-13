import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import Select from '../../components/ui/Select'
import Button from '../../components/ui/Button'
import QueryBoundary from '../../components/ui/QueryBoundary'
import JobForm from './JobForm'
import { jobsApi } from '../../api/jobs'
import { useSave } from '../../hooks/useApi'
export default function EditJob() {
  const { id } = useParams()
  const nav = useNavigate()
  const q = useQuery({ queryKey: ['job', id], queryFn: () => jobsApi.get(id) })
  const update = useSave(jobsApi.update, { invalidate: [['job', id], ['jobs']], success: 'Job saved' })
  const status = useSave(jobsApi.setStatus, { invalidate: [['job', id], ['jobs']], success: 'Status updated' })
  const remove = useSave(jobsApi.remove, { invalidate: [['jobs']], success: 'Job deleted', onSuccess: () => nav('/jobs') })
  return (
    <div className="max-w-3xl">
      <Breadcrumbs items={[['Jobs', '/jobs'], ['Job', `/jobs/${id}`], ['Edit']]} />
      <PageHeader title="Edit job" />
      <QueryBoundary q={q}>{(job) => (
        <div className="space-y-4">
          <Card><CardBody className="flex flex-wrap items-end gap-3">
            <Select label="Status" value={job.status || ''} onChange={(e) => status.mutate({ id, status: e.target.value })} disabled={status.isPending} className="w-48">
              {['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'CANCELLED'].map((s) => <option key={s} value={s}>{s.replace('_', ' ').toLowerCase().replace(/^./, (c) => c.toUpperCase())}</option>)}
            </Select>
            <Button variant="destructive" loading={remove.isPending} onClick={() => window.confirm('Delete this job? This cannot be undone.') && remove.mutate(id)}>Delete job</Button>
          </CardBody></Card>
          <Card><CardBody><JobForm job={job} onSubmit={(v) => update.mutateAsync({ id, ...v })} submitLabel="Save job" onCancel={() => nav(`/jobs/${id}`)} /></CardBody></Card>
        </div>)}
      </QueryBoundary>
    </div>
  )
}
