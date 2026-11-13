import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { GraduationCap } from 'lucide-react'
import { academyApi } from '../../api/academy'
import PageHeader from '../../components/ui/PageHeader'
import Select from '../../components/ui/Select'
import Card from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import ErrorState from '../../components/ui/ErrorState'
import { SkeletonList } from '../../components/ui/Skeleton'

const name = (x) => x.name || x.title || `#${x.id}`
export default function Courses() {
  const [programId, setProgram] = useState('')
  const [departmentId, setDept] = useState('')
  const programs = useQuery({ queryKey: ['programs'], queryFn: academyApi.programs })
  const depts = useQuery({ queryKey: ['departments', programId], queryFn: () => academyApi.departments(programId), enabled: !!programId })
  const courses = useQuery({ queryKey: ['courses', departmentId], queryFn: () => academyApi.courses(departmentId), enabled: !!departmentId })
  const list = (d) => (Array.isArray(d) ? d : d?.content || [])
  return (
    <div>
      <PageHeader title="Course catalog" description="Choose a program, then a department, to see its courses." />
      {programs.isError && <ErrorState error={programs.error} onRetry={programs.refetch} />}
      <div className="mb-6 grid gap-3 sm:grid-cols-2">
        <Select label="Program" value={programId} onChange={(e) => { setProgram(e.target.value); setDept('') }}>
          <option value="">{programs.isLoading ? 'Loading…' : 'Select a program'}</option>
          {list(programs.data).map((p) => <option key={p.id} value={p.id}>{name(p)}</option>)}
        </Select>
        <Select label="Department" value={departmentId} disabled={!programId} onChange={(e) => setDept(e.target.value)}>
          <option value="">{programId ? 'Select a department' : 'Pick a program first'}</option>
          {list(depts.data).map((d) => <option key={d.id} value={d.id}>{name(d)}</option>)}
        </Select>
      </div>
      {!departmentId ? <EmptyState icon={GraduationCap} title="Nothing selected yet" description="Pick a program and department to browse courses." />
        : courses.isLoading ? <SkeletonList rows={3} />
        : courses.isError ? <ErrorState error={courses.error} onRetry={courses.refetch} />
        : list(courses.data).length === 0 ? <EmptyState icon={GraduationCap} title="No courses in this department yet" />
        : <div className="grid gap-3 sm:grid-cols-2">{list(courses.data).map((c) => (
            <Link key={c.id} to={`/courses/${c.id}`} state={{ course: c }}><Card className="h-full p-4 hover:border-primary/60"><h3 className="font-semibold">{name(c)}</h3>{c.code && <p className="font-mono text-xs text-muted">{c.code}</p>}{c.description && <p className="mt-2 line-clamp-3 text-sm text-muted">{c.description}</p>}</Card></Link>))}</div>}
    </div>
  )
}
