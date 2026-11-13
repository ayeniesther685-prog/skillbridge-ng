import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import PageHeader from '../../components/ui/PageHeader'
import Card, { CardBody } from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import QueryBoundary from '../../components/ui/QueryBoundary'
import CoursePicker from './CoursePicker'
import { academyApi } from '../../api/academy'
import { useSave } from '../../hooks/useApi'
export default function AcademyEnroll() {
  const [sel, setSel] = useState({})
  const rec = useQuery({ queryKey: ['academy', 'record'], queryFn: academyApi.record, retry: false })
  const enroll = useSave(academyApi.enroll, { invalidate: [['academy']], success: 'You’re enrolled' })
  return (
    <div className="max-w-xl">
      <PageHeader title="Enroll as a student" description="Join a program to access courses, assignments and certificates." />
      {rec.isLoading ? <QueryBoundary q={rec}>{() => null}</QueryBoundary> : rec.data ? (
        <Card><CardBody><h3 className="font-semibold">You’re enrolled</h3>
          <pre className="mt-2 overflow-x-auto font-mono text-xs text-muted">{JSON.stringify(rec.data, null, 2)}</pre>
          <Button as={Link} to="/academy/courses" className="mt-4" size="sm">Go to my courses</Button></CardBody></Card>
      ) : (
        <Card><CardBody className="space-y-4">
          <CoursePicker withCourse={false} onChange={setSel} />
          <Button disabled={!sel.programId} loading={enroll.isPending} onClick={() => enroll.mutate({ programId: Number(sel.programId), ...(sel.departmentId ? { departmentId: Number(sel.departmentId) } : {}) })}>Enroll</Button>
        </CardBody></Card>)}
    </div>
  )
}
