import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import DataTable from '../../components/ui/DataTable'
import EmptyState from '../../components/ui/EmptyState'
import StatusBadge from '../../components/ui/StatusBadge'
import QueryBoundary from '../../components/ui/QueryBoundary'
import Button from '../../components/ui/Button'
import { adminApi } from '../../api/admin'
import { list } from '../academy/CoursePicker'
import { nameOf } from '../../utils/ids'
import { fmtDate } from '../../utils/format'
export default function Submissions() {
  const { id } = useParams(); const nav = useNavigate()
  const q = useQuery({ queryKey: ['admin', 'submissions', id], queryFn: () => adminApi.submissions(id) })
  return (
    <div>
      <Breadcrumbs items={[['Courses', '/admin/courses'], ['Submissions']]} />
      <PageHeader title="Submissions" />
      <QueryBoundary q={q}>{(d) => list(d).length === 0 ? <EmptyState title="No submissions yet" /> :
        <DataTable rows={list(d)} columns={[
          { key: 'who', header: 'Student', render: (s) => nameOf(s.student || s.user) || (s.studentId ? `User #${s.studentId}` : '—') },
          { key: 'at', header: 'Submitted', render: (s) => fmtDate(s.submittedAt || s.createdAt) },
          { key: 'score', header: 'Score', render: (s) => s.score ?? '—' },
          { key: 'status', header: 'Status', render: (s) => s.status ? <StatusBadge status={s.status} /> : '—' },
          { key: 'act', header: '', render: (s) => <Button size="sm" variant="outline" onClick={() => nav(`/admin/submissions/${s.id}/grade`, { state: { submission: s } })}>Grade</Button> },
        ]} />}</QueryBoundary>
    </div>
  )
}
