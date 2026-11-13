import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import StatusBadge from '../../components/ui/StatusBadge'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { academyApi } from '../../api/academy'
import { useSave } from '../../hooks/useApi'
export default function CourseProgress() {
  const { id } = useParams()
  const q = useQuery({ queryKey: ['progress', id], queryFn: () => academyApi.courseProgress(id) })
  const cert = useSave(academyApi.issueCertificate, { invalidate: [['certificates']], success: 'Certificate issued' })
  return (
    <div className="max-w-2xl">
      <Breadcrumbs items={[['My courses', '/academy/courses'], [`Course #${id}`, `/academy/courses/${id}`], ['Progress']]} />
      <PageHeader title="Course progress" />
      <QueryBoundary q={q}>{(p) => {
        const pct = Math.round(p.progressPercent ?? p.percentage ?? p.percent ?? p.completionPercentage ?? (p.totalLessons ? (100 * (p.completedLessons ?? 0)) / p.totalLessons : 0))
        const lessons = p.lessons || p.lessonProgress || []
        return (
          <div className="space-y-4">
            <Card><CardBody>
              <div className="mb-2 flex justify-between text-sm"><span>Completed</span><span className="font-semibold">{pct}%</span></div>
              <div className="h-2 overflow-hidden rounded bg-fg/10"><div className="h-full bg-primary" style={{ width: `${Math.min(100, pct)}%` }} /></div>
              {p.completedLessons != null && p.totalLessons != null && <p className="mt-2 text-xs text-muted">{p.completedLessons} of {p.totalLessons} lessons</p>}
            </CardBody></Card>
            {lessons.length > 0 && <Card><CardBody className="space-y-2 text-sm">{lessons.map((l, i) => <div key={l.lessonId ?? l.id ?? i} className="flex items-center justify-between"><span>{l.title || l.lessonTitle || `Lesson ${l.lessonId ?? l.id}`}</span><StatusBadge status={l.status} /></div>)}</CardBody></Card>}
            <div className="flex gap-2">
              <Button loading={cert.isPending} disabled={pct < 100} onClick={() => cert.mutate(id)}>Get certificate</Button>
              <Button as={Link} to="/academy/certificates" variant="outline">My certificates</Button>
            </div>
            {pct < 100 && <p className="text-xs text-muted">Complete every lesson to unlock your certificate.</p>}
          </div>)
      }}</QueryBoundary>
    </div>
  )
}
