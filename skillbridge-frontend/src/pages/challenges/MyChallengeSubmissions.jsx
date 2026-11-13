import { useQuery } from '@tanstack/react-query'
import PageHeader from '../../components/ui/PageHeader'
import DataTable from '../../components/ui/DataTable'
import EmptyState from '../../components/ui/EmptyState'
import StatusBadge from '../../components/ui/StatusBadge'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { challengesApi } from '../../api/challenges'
import { list } from '../academy/CoursePicker'
import { fmtDate } from '../../utils/format'
export default function MyChallengeSubmissions() {
  const q = useQuery({ queryKey: ['challenges', 'mine'], queryFn: challengesApi.mine })
  return (
    <div><PageHeader title="My challenge submissions" />
      <QueryBoundary q={q}>{(d) => list(d).length === 0 ? <EmptyState title="No submissions yet" description="Pick a challenge and send your first solution." /> :
        <DataTable rows={list(d)} columns={[
          { key: 'ch', header: 'Challenge', render: (s) => s.challengeTitle || s.challenge?.title || `#${s.challengeId}` },
          { key: 'at', header: 'Submitted', render: (s) => fmtDate(s.submittedAt || s.createdAt) },
          { key: 'status', header: 'Status', render: (s) => <StatusBadge status={s.status} /> },
        ]} />}</QueryBoundary></div>
  )
}
