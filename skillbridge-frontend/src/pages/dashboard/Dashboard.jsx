import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Bell, FileText, FileSignature, GraduationCap } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import StatCard from '../../components/ui/StatCard'
import Button from '../../components/ui/Button'
import ComingSoon from '../../components/ui/ComingSoon'
import EmptyState from '../../components/ui/EmptyState'
import Card, { CardBody } from '../../components/ui/Card'
import { useAuth } from '../../context/AuthContext'
import { notificationsApi } from '../../api/notifications'
import { applicationsApi, contractsApi } from '../../api/contracts'
import { academyApi } from '../../api/academy'
import { normalizeError } from '../../api/errors'

const blurb = {
  STUDENT: 'Your learning and applications at a glance.',
  FREELANCER: 'Your applications and contracts at a glance.',
  CLIENT: 'Your hiring activity at a glance.',
  ADMIN: 'Administration tools for the Academy and AI quotas.',
}
export default function Dashboard({ role }) {
  const { user } = useAuth()
  const unread = useQuery({ queryKey: ['notifications', 'unread'], queryFn: notificationsApi.unreadCount })
  const contracts = useQuery({ queryKey: ['contracts', 'count'], queryFn: () => contractsApi.list({ page: 0, size: 1 }), enabled: role !== 'ADMIN' })
  const apps = useQuery({ queryKey: ['applications', 'count'], queryFn: () => applicationsApi.list({ page: 0, size: 1 }), enabled: role === 'STUDENT' || role === 'FREELANCER' })
  const record = useQuery({ queryKey: ['academy', 'record'], queryFn: academyApi.record, enabled: role === 'STUDENT', retry: false })
  const noRecord = record.isError && normalizeError(record.error).status === 404
  return (
    <div>
      <PageHeader title={`Welcome, ${user.firstName}`} description={blurb[role]} actions={<Button as={Link} to="/jobs" variant="outline" size="sm">Browse jobs</Button>} />
      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Unread notifications" icon={Bell} value={unread.data?.count ?? '—'} loading={unread.isLoading} />
        {role !== 'ADMIN' && <StatCard label="Contracts" icon={FileSignature} value={contracts.data?.totalElements ?? '—'} loading={contracts.isLoading} />}
        {(role === 'STUDENT' || role === 'FREELANCER') && <StatCard label="Applications" icon={FileText} value={apps.data?.totalElements ?? '—'} loading={apps.isLoading} />}
      </div>
      <div className="mt-6 space-y-4">
        {role === 'STUDENT' && (noRecord
          ? <EmptyState icon={GraduationCap} title="You haven't enrolled in a program" description="Enrolment opens in the Academy pages, which land in a later build phase. You can browse the catalog now." action={<Button as={Link} to="/courses" size="sm">Browse courses</Button>} />
          : record.data && <Card><CardBody><h3 className="font-semibold">Academy record</h3><pre className="mt-2 overflow-x-auto font-mono text-xs text-muted">{JSON.stringify(record.data, null, 2)}</pre></CardBody></Card>)}
        {role === 'ADMIN' && <>
          <div className="grid gap-3 sm:grid-cols-3">
            {[['/admin/courses', 'Manage courses', 'Add lessons and assignments, review submissions'], ['/admin/courses/new', 'Create a course', 'Add a course to a department'], ['/admin/ai-quota', 'AI quota', 'Grant credits or change a tier']].map(([to, t, d]) => (
              <Link key={to} to={to}><Card className="h-full p-4 hover:border-primary/60"><h3 className="font-semibold">{t}</h3><p className="mt-1 text-sm text-muted">{d}</p></Card></Link>))}
          </div>
          <ComingSoon title="Platform statistics" description="Totals for users, enrolments and revenue." endpoint="GET /api/admin/stats" />
          <ComingSoon title="User management" description="Suspend, delete or change the role of users." endpoint="/api/admin/users" />
        </>}
        {(role === 'CLIENT') && <div className="flex gap-2"><Button as={Link} to="/jobs/new" size="sm">Post a job</Button><Button as={Link} to="/contracts" variant="outline" size="sm">View contracts</Button></div>}
      </div>
    </div>
  )
}
