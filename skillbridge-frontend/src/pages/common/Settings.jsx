import PageHeader from '../../components/ui/PageHeader'
import Card, { CardBody } from '../../components/ui/Card'
import Badge from '../../components/ui/Badge'
import ComingSoon from '../../components/ui/ComingSoon'
import { useAuth } from '../../context/AuthContext'
export default function Settings() {
  const { user } = useAuth()
  const rows = [['Name', `${user.firstName} ${user.lastName}`], ['Email', user.email], ['Phone', user.phone || '—'], ['Location', user.location || '—']]
  return (
    <div className="max-w-2xl space-y-4">
      <PageHeader title="Settings" description="Account details are read-only for now." />
      <Card><CardBody className="space-y-3 text-sm">
        {rows.map(([k, v]) => <div key={k} className="flex justify-between gap-4"><span className="text-muted">{k}</span><span>{v}</span></div>)}
        <div className="flex justify-between gap-4"><span className="text-muted">Roles</span><span className="flex gap-1">{user.roles.map((r) => <Badge key={r} variant="info">{r}</Badge>)}</span></div>
      </CardBody></Card>
      <ComingSoon title="Change password & verify email" description="Reset your password and confirm your email address." />
    </div>
  )
}
