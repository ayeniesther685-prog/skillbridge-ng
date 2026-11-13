import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { academyApi } from '../../api/academy'
import { useSave } from '../../hooks/useApi'
import { list } from './CoursePicker'
export default function LessonPlayer() {
  const { id, lessonId } = useParams()
  const lesson = useQuery({ queryKey: ['lesson', id, lessonId], queryFn: () => academyApi.lesson(id, lessonId) })
  const all = useQuery({ queryKey: ['lessons', id], queryFn: () => academyApi.lessons(id) })
  const prog = useSave(academyApi.setProgress, { invalidate: [['progress', id]], success: 'Progress saved' })
  const lessons = list(all.data); const idx = lessons.findIndex((l) => String(l.id) === lessonId)
  const prev = lessons[idx - 1], next = lessons[idx + 1]
  return (
    <QueryBoundary q={lesson}>{(l) => (
      <div className="max-w-3xl">
        <Breadcrumbs items={[['My courses', '/academy/courses'], [`Course #${id}`, `/academy/courses/${id}`], [l.title]]} />
        <PageHeader title={l.title} />
        {l.videoUrl && <p className="mb-4 text-sm"><a className="text-accent hover:underline" href={l.videoUrl} target="_blank" rel="noreferrer noopener">Open lesson video</a></p>}
        <Card><CardBody><div className="whitespace-pre-line text-sm leading-relaxed">{l.content || l.body || 'This lesson has no written content.'}</div></CardBody></Card>
        <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
          <div className="flex gap-2">{prev && <Button as={Link} to={`/academy/courses/${id}/lessons/${prev.id}`} variant="outline" size="sm">Previous</Button>}{next && <Button as={Link} to={`/academy/courses/${id}/lessons/${next.id}`} variant="outline" size="sm">Next lesson</Button>}</div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled={prog.isPending} onClick={() => prog.mutate({ lessonId, status: 'IN_PROGRESS' })}>Mark in progress</Button>
            <Button size="sm" loading={prog.isPending} onClick={() => prog.mutate({ lessonId, status: 'COMPLETED' })}>Mark completed</Button>
          </div>
        </div>
      </div>)}
    </QueryBoundary>
  )
}
