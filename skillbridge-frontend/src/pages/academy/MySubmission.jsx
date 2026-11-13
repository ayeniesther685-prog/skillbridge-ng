import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import StatusBadge from '../../components/ui/StatusBadge'
import { SkeletonList } from '../../components/ui/Skeleton'
import ErrorState from '../../components/ui/ErrorState'
import { academyApi } from '../../api/academy'
import { normalizeError } from '../../api/errors'
import { fmtDate } from '../../utils/format'
export default function MySubmission() {
  const { id } = useParams()
  const q = useQuery({ queryKey: ['submission', id], queryFn: () => academyApi.mySubmission(id), retry: false })
  const none = q.isError && normalizeError(q.error).status === 404
  return (
    <div className="max-w-2xl">
      <Breadcrumbs items={[['Assignment', `/academy/assignments/${id}`], ['My submission']]} />
      <PageHeader title="My submission" />
      {q.isLoading ? <SkeletonList rows={2} /> : none ? <EmptyState title="You haven’t submitted yet" action={<Button as={Link} to={`/academy/assignments/${id}`} size="sm">Submit now</Button>} />
        : q.isError ? <ErrorState error={q.error} onRetry={q.refetch} /> : (
        <Card><CardBody className="space-y-3 text-sm">
          {q.data.status && <StatusBadge status={q.data.status} />}
          {q.data.content && <p className="whitespace-pre-line">{q.data.content}</p>}
          {q.data.fileUrl && <a className="text-accent hover:underline" href={q.data.fileUrl} target="_blank" rel="noreferrer noopener">Open submitted link</a>}
          <p className="text-xs text-muted">Submitted {fmtDate(q.data.submittedAt || q.data.createdAt)}</p>
          {(q.data.score != null || q.data.feedback) && <div className="rounded border p-3"><p className="font-medium">Grade{q.data.score != null ? `: ${q.data.score}` : ''}</p>{q.data.feedback && <p className="mt-1 text-muted">{q.data.feedback}</p>}</div>}
        </CardBody></Card>)}
    </div>
  )
}
