import { Bell } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import Pagination from '../../components/ui/Pagination'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { notificationsApi } from '../../api/notifications'
import { usePaged, useSave } from '../../hooks/useApi'
import { fmtDate } from '../../utils/format'
export default function Notifications() {
  const { q, setPage } = usePaged(['notifications', 'list'], notificationsApi.list)
  const inv = { invalidate: [['notifications']] }
  const read = useSave(notificationsApi.read, inv)
  const all = useSave(notificationsApi.readAll, { ...inv, success: 'All marked as read' })
  return (
    <div>
      <PageHeader title="Notifications" actions={<Button variant="outline" size="sm" loading={all.isPending} onClick={() => all.mutate()}>Mark all read</Button>} />
      <QueryBoundary q={q}>{(d) => d.content.length === 0 ? <EmptyState icon={Bell} title="You're all caught up" description="New activity will show up here." /> :
        <><div className="space-y-2">{d.content.map((n) => { const unread = !(n.read ?? n.isRead); return (
          <Card key={n.id} className={`flex items-start justify-between gap-3 p-4 ${unread ? 'border-primary/50' : ''}`}>
            <div className="text-sm"><p className={unread ? 'font-medium' : ''}>{n.title || n.message}</p>{n.title && n.message && <p className="text-muted">{n.message}</p>}<p className="mt-1 text-xs text-muted">{fmtDate(n.createdAt)}</p></div>
            {unread && <Button size="sm" variant="ghost" disabled={read.isPending} onClick={() => read.mutate(n.id)}>Mark read</Button>}
          </Card>) })}</div><Pagination page={d} onPage={setPage} /></>}</QueryBoundary>
    </div>
  )
}
