import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import ComingSoon from '../../components/ui/ComingSoon'
import QueryBoundary from '../../components/ui/QueryBoundary'
import CoursePicker, { list } from '../academy/CoursePicker'
import { academyApi } from '../../api/academy'
export default function AdminCourses() {
  const [sel, setSel] = useState({})
  const a = useQuery({ queryKey: ['assignments', sel.courseId], queryFn: () => academyApi.assignments(sel.courseId), enabled: !!sel.courseId })
  return (
    <div>
      <PageHeader title="Courses" description="Pick a course to add lessons and assignments or review submissions." actions={<Button as={Link} to="/admin/courses/new" size="sm">New course</Button>} />
      <Card className="mb-6"><CardBody><CoursePicker onChange={setSel} /></CardBody></Card>
      {sel.courseId && <div className="space-y-4">
        <div className="flex flex-wrap gap-2"><Button as={Link} to={`/admin/courses/${sel.courseId}/lessons/new`} size="sm">Add lesson</Button><Button as={Link} to={`/admin/courses/${sel.courseId}/assignments/new`} variant="outline" size="sm">Add assignment</Button></div>
        <h2 className="font-semibold">Assignments</h2>
        <QueryBoundary q={a} rows={2}>{(d) => list(d).length === 0 ? <p className="text-sm text-muted">No assignments yet.</p> :
          <div className="space-y-2">{list(d).map((x) => <Card key={x.id}><CardBody className="flex items-center justify-between py-3 text-sm"><span>{x.title}</span><Button as={Link} to={`/admin/assignments/${x.id}/submissions`} variant="outline" size="sm">Submissions</Button></CardBody></Card>)}</div>}</QueryBoundary>
      </div>}
      <div className="mt-8"><ComingSoon title="Edit or delete courses and lessons" description="Change or remove content after it's created." /></div>
    </div>
  )
}
