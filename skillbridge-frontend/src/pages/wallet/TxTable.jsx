import DataTable from '../../components/ui/DataTable'
import EmptyState from '../../components/ui/EmptyState'
import Badge from '../../components/ui/Badge'
import { fmtDate, label } from '../../utils/format'
export default function TxTable({ rows }) {
  if (!rows?.length) return <EmptyState title="No transactions yet" description="Points you earn or spend will appear here." />
  const amt = (t) => t.amountPoints ?? t.points ?? t.amount
  return <DataTable rows={rows} columns={[
    { key: 'createdAt', header: 'Date', render: (t) => fmtDate(t.createdAt) },
    { key: 'type', header: 'Type', render: (t) => <Badge variant={/CREDIT|EARN|REWARD/i.test(t.type) ? 'success' : 'neutral'}>{label(t.type)}</Badge> },
    { key: 'description', header: 'Description', render: (t) => t.description || t.reason || '—' },
    { key: 'amount', header: 'Points', render: (t) => (amt(t) ?? '—') },
  ]} />
}
