import { useState } from 'react'
import { Link } from 'react-router-dom'
import { FileSignature } from 'lucide-react'
import * as contractApi from '../api/contractApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { formatDate, formatMoney } from '../utils/dates.js'
import { Badge, Card, PageHeader } from '../components/ui.jsx'
import Pagination from '../components/Pagination.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import EmptyState from '../components/EmptyState.jsx'

export default function Contracts() {
  const { user } = useAuth()
  const [page, setPage] = useState(0)
  const { data, loading, error, reload } = useFetch(() => contractApi.list({ page, size: 10 }), [page])
  return (
    <div>
      <PageHeader title="Contracts" subtitle="Work agreements where you are the client or the freelancer." />
      {loading ? <LoadingSpinner /> : error ? <ErrorMessage error={error} onRetry={reload} /> : !data.content.length ? (
        <EmptyState icon={FileSignature} title="No contracts yet" text="Contracts are created when a client accepts an application." />
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2">
            {data.content.map((c) => (
              <Link key={c.id} to={`/contracts/${c.id}`}>
                <Card className="h-full transition-colors hover:border-brand-400/60">
                  <div className="flex items-start justify-between gap-3"><h2 className="font-extrabold">{c.title || c.jobTitle}</h2><Badge value={c.status} /></div>
                  <p className="mt-1 text-sm text-muted">{c.clientId === user.id ? `Freelancer: ${c.freelancerName}` : `Client: ${c.clientName}`}</p>
                  <p className="mt-3 text-lg font-extrabold">{formatMoney(c.amount)}</p>
                  <p className="text-xs text-muted">{c.milestones?.length ?? 0} milestones · Started {formatDate(c.startDate ?? c.createdAt)}</p>
                </Card>
              </Link>
            ))}
          </div>
          <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
