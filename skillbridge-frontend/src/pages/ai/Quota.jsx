import { useQuery } from '@tanstack/react-query'
import { aiApi } from '../../api/ai'
import { usePaged } from '../../hooks/useApi'
import QueryBoundary from '../../components/ui/QueryBoundary'
import Card, { CardBody } from '../../components/ui/Card'
import StatCard from '../../components/ui/StatCard'
import DataTable from '../../components/ui/DataTable'
import Pagination from '../../components/ui/Pagination'
import EmptyState from '../../components/ui/EmptyState'
import { fmtDate } from '../../utils/format'
export default function Quota() {
  const quota = useQuery({ queryKey: ['ai', 'quota'], queryFn: aiApi.quota })
  const tiers = useQuery({ queryKey: ['ai', 'tiers'], queryFn: aiApi.tiers })
  const { q: usage, setPage } = usePaged(['ai', 'usage'], aiApi.usage)
  const tl = Array.isArray(tiers.data) ? tiers.data : tiers.data?.content || []
  return (
    <div className="space-y-6">
      <QueryBoundary q={quota} rows={1}>{(d) => (
        <div className="grid gap-4 sm:grid-cols-3">
          {Object.entries(d).filter(([, v]) => v != null && typeof v !== 'object').map(([k, v]) => <StatCard key={k} label={k.replace(/([A-Z])/g, ' $1').replace(/^./, (c) => c.toUpperCase())} value={/at$/i.test(k) && !Number.isFinite(v) ? fmtDate(v) : String(v)} />)}
        </div>)}</QueryBoundary>
      {tl.length > 0 && <Card><CardBody><h3 className="mb-2 font-semibold">Tiers</h3><ul className="space-y-1 text-sm text-muted">{tl.map((t, i) => <li key={t.id ?? i}><span className="text-fg">{t.name ?? t.tierName}</span>{t.dailyLimit != null ? ` — ${t.dailyLimit} requests/day` : ''}</li>)}</ul></CardBody></Card>}
      <div><h3 className="mb-3 font-semibold">Usage history</h3>
        <QueryBoundary q={usage}>{(d) => d.content.length === 0 ? <EmptyState title="No AI usage yet" description="Ask the tutor a question to get started." /> :
          <><DataTable rows={d.content} columns={[{ key: 'at', header: 'When', render: (u) => fmtDate(u.createdAt) }, { key: 'tokens', header: 'Tokens', render: (u) => u.tokensUsed ?? u.tokens ?? '—' }, { key: 'type', header: 'Type', render: (u) => u.type || u.feature || '—' }]} /><Pagination page={d} onPage={setPage} /></>}</QueryBoundary></div>
    </div>
  )
}
