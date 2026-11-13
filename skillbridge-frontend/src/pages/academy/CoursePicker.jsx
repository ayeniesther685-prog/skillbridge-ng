import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { academyApi } from '../../api/academy'
import Select from '../../components/ui/Select'
export const list = (d) => (Array.isArray(d) ? d : d?.content || [])
export const nm = (x) => x.name || x.title || `#${x.id}`
// Program → department → (optional) course cascading picker
export default function CoursePicker({ onChange, withCourse = true }) {
  const [p, setP] = useState(''); const [d, setD] = useState('')
  const programs = useQuery({ queryKey: ['programs'], queryFn: academyApi.programs })
  const depts = useQuery({ queryKey: ['departments', p], queryFn: () => academyApi.departments(p), enabled: !!p })
  const courses = useQuery({ queryKey: ['courses', d], queryFn: () => academyApi.courses(d), enabled: !!d && withCourse })
  return (
    <div className="space-y-3">
      <Select label="Program" value={p} onChange={(e) => { setP(e.target.value); setD(''); onChange({ programId: e.target.value }) }}><option value="">Select a program</option>{list(programs.data).map((x) => <option key={x.id} value={x.id}>{nm(x)}</option>)}</Select>
      <Select label="Department" value={d} disabled={!p} onChange={(e) => { setD(e.target.value); onChange({ programId: p, departmentId: e.target.value }) }}><option value="">Select a department</option>{list(depts.data).map((x) => <option key={x.id} value={x.id}>{nm(x)}</option>)}</Select>
      {withCourse && <Select label="Course" disabled={!d} onChange={(e) => onChange({ programId: p, departmentId: d, courseId: e.target.value })}><option value="">Select a course</option>{list(courses.data).map((x) => <option key={x.id} value={x.id}>{nm(x)}</option>)}</Select>}
    </div>
  )
}
