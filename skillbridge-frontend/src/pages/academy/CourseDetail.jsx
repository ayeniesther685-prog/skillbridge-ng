import { Link, useLocation, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { BookOpen, ClipboardList } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { academyApi } from '../../api/academy'
import { useAuth } from '../../context/AuthContext'
import { list } from './CoursePicker'
import { fmtDate } from '../../utils/format'

// Serves both /courses/:id (public preview) and /academy/courses/:id (enrolled view)
export default function CourseDetail() {
  const { id } = useParams()
  const { state } = useLocation()
  const { user } = useAuth()
  const lessons = useQuery({ queryKey: ['lessons', id], queryFn: () => academyApi.lessons(id) })
  const assignments = useQuery({ queryKey: ['assignments', id], queryFn: () => academyApi.assignments(id) })
  const course = state?.course
  return (
    <div>
      <Breadcrumbs items={[[user ? 'My courses' : 'Courses', user ? '/academy/courses' : '/courses'], [course?.title || course?.name || `Course #${id}`]]} />
      <PageHeader title={course?.title || course?.name || `Course #${id}`} description={course?.description}
        actions={user ? <Button as={Link} to={`/academy/courses/${id}/progress`} variant="outline" size="sm">My progress</Button> : <Button as={Link} to={`/login?next=/courses/${id}`} size="sm">Log in to enroll</Button>} />
      <h2 className="mb-3 font-semibold">Lessons</h2>
      <QueryBoundary q={lessons} rows={3}>{(d) => list(d).length === 0 ? <EmptyState icon={BookOpen} title="No lessons yet" /> :
        <div className="space-y-2">{list(d).map((l, i) => (
          <Card key={l.id}><CardBody className="flex items-center justify-between gap-3 py-3 text-sm">
            <span><span className="mr-2 text-muted">{i + 1}.</span>{l.title}</span>
            {user ? <Button as={Link} to={`/academy/courses/${id}/lessons/${l.id}`} variant="outline" size="sm">Open</Button> : <span className="text-xs text-muted">Log in to view</span>}
          </CardBody></Card>))}</div>}</QueryBoundary>
      <h2 className="mb-3 mt-8 font-semibold">Assignments</h2>
      <QueryBoundary q={assignments} rows={2}>{(d) => list(d).length === 0 ? <EmptyState icon={ClipboardList} title="No assignments yet" /> :
        <div className="space-y-2">{list(d).map((a) => (
          <Card key={a.id}><CardBody className="flex items-center justify-between gap-3 py-3 text-sm">
            <span>{a.title}{a.dueDate && <span className="ml-2 text-xs text-muted">due {fmtDate(a.dueDate)}</span>}</span>
            {user && <Button as={Link} to={`/academy/assignments/${a.id}`} variant="outline" size="sm">Open</Button>}
          </CardBody></Card>))}</div>}</QueryBoundary>
    </div>
  )
}
