import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft } from 'lucide-react'
import { jobsApi } from '../../api/jobs'
import Card, { CardBody } from '../../components/ui/Card'
import Badge from '../../components/ui/Badge'
import StatusBadge from '../../components/ui/StatusBadge'
import ErrorState from '../../components/ui/ErrorState'
import Button from '../../components/ui/Button'
import Skeleton from '../../components/ui/Skeleton'
import { useAuth } from '../../context/AuthContext'
import { fmtDate, label } from '../../utils/format'
import { jobBudget } from './jobUtils'

export default function JobDetail() {
  const { id } = useParams()
  const { user, hasRole } = useAuth()
  const q = useQuery({ queryKey: ['job', id], queryFn: () => jobsApi.get(id) })
  if (q.isLoading) return <div className="space-y-3"><Skeleton className="h-8 w-1/2" /><Skeleton variant="card" /></div>
  if (q.isError) return <ErrorState error={q.error} onRetry={q.refetch} />
  const j = q.data
  return (
    <div>
      <Link to="/jobs" className="mb-4 inline-flex items-center gap-1 text-sm text-muted hover:text-fg"><ArrowLeft className="h-4 w-4" />All jobs</Link>
      <div className="mb-4 flex flex-wrap items-center gap-3"><h1 className="text-2xl font-semibold tracking-tight">{j.title}</h1>{j.status && <StatusBadge status={j.status} />}</div>
      <div className="mb-6 flex flex-wrap gap-2">{j.jobType && <Badge variant="info">{label(j.jobType)}</Badge>}{j.experienceLevel && <Badge>{label(j.experienceLevel)}</Badge>}{j.location && <Badge>{j.location}</Badge>}</div>
      <div className="grid gap-6 md:grid-cols-3">
        <Card className="md:col-span-2"><CardBody><h2 className="mb-2 font-semibold">About this job</h2><p className="whitespace-pre-line text-sm leading-relaxed text-muted">{j.description}</p></CardBody></Card>
        <div className="space-y-4">
          <Card><CardBody className="space-y-2 text-sm">
            {jobBudget(j) && <p><span className="text-muted">Budget </span>{jobBudget(j)}</p>}
            {j.createdAt && <p><span className="text-muted">Posted </span>{fmtDate(j.createdAt)}</p>}
            {j.deadline && <p><span className="text-muted">Deadline </span>{fmtDate(j.deadline)}</p>}
          </CardBody></Card>
          {!user && <Button as={Link} to={`/login?next=/jobs/${id}`} className="w-full">Log in to apply</Button>}
          {user && hasRole('STUDENT', 'FREELANCER') && <Button as={Link} to={`/jobs/${id}/apply`} className="w-full" disabled={j.status && j.status !== 'OPEN'}>Apply for this job</Button>}
          {user && hasRole('CLIENT') && <div className="space-y-2"><Button as={Link} to={`/jobs/${id}/applications`} className="w-full">View applicants</Button><Button as={Link} to={`/jobs/${id}/edit`} variant="outline" className="w-full">Edit job</Button></div>}
        </div>
      </div>
    </div>
  )
}
