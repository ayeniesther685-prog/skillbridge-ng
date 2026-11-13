import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import { GraduationCap } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import Dialog from '../../components/ui/Dialog'
import Input from '../../components/ui/Input'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import CoursePicker, { list } from './CoursePicker'
import { academyApi } from '../../api/academy'
import { useSave } from '../../hooks/useApi'
export default function AcademyCourses() {
  const [open, setOpen] = useState(false); const [sel, setSel] = useState({}); const [sem, setSem] = useState('')
  const q = useQuery({ queryKey: ['academy', 'enrollments'], queryFn: academyApi.enrollments })
  const enroll = useSave(academyApi.enrollCourse, { invalidate: [['academy']], success: 'Enrolled in course', onSuccess: () => setOpen(false) })
  const ok = sel.courseId && z.coerce.number().int().positive().safeParse(sem).success
  return (
    <div>
      <PageHeader title="My courses" description="Courses you're enrolled in this term." actions={<><Button as={Link} to="/courses" variant="outline" size="sm">Course catalog</Button><Button size="sm" onClick={() => setOpen(true)}>Enroll in a course</Button></>} />
      <QueryBoundary q={q}>{(d) => list(d).length === 0
        ? <EmptyState icon={GraduationCap} title="No course enrollments yet" description="Enroll in a program first, then add courses for a semester." action={<div className="flex gap-2"><Button as={Link} to="/academy/enroll" variant="outline" size="sm">Enroll in a program</Button><Button size="sm" onClick={() => setOpen(true)}>Add a course</Button></div>} />
        : <div className="grid gap-3 sm:grid-cols-2">{list(d).map((e) => { const cid = e.courseId ?? e.course?.id; const course = e.course || { id: cid, title: e.courseTitle || e.courseName }; return (
            <Link key={e.id ?? cid} to={`/academy/courses/${cid}`} state={{ course }}><Card className="p-4 hover:border-primary/60"><h3 className="font-semibold">{course.title || course.name || `Course #${cid}`}</h3>{e.status && <p className="mt-1 text-xs text-muted">{e.status}</p>}</Card></Link>) })}</div>}</QueryBoundary>
      <Dialog open={open} onClose={() => setOpen(false)} title="Enroll in a course">
        <div className="space-y-4">
          <CoursePicker onChange={setSel} />
          <Input label="Semester ID" type="number" value={sem} onChange={(e) => setSem(e.target.value)} hint="Enter your current semester’s ID — the API has no semester list yet." />
          <div className="flex justify-end gap-2"><Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button><Button disabled={!ok} loading={enroll.isPending} onClick={() => enroll.mutate({ courseId: Number(sel.courseId), semesterId: Number(sem) })}>Enroll</Button></div>
        </div>
      </Dialog>
    </div>
  )
}
