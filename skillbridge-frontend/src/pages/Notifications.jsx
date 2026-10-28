import { useState } from 'react'
import { Bell, CheckCheck } from 'lucide-react'
import * as notificationApi from '../api/notificationApi.js'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { timeAgo } from '../utils/dates.js'
import { Badge, Button, PageHeader } from '../components/ui.jsx'
import Pagination from '../components/Pagination.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { useToast } from '../components/Toast.jsx'

export default function Notifications() {
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [busy, setBusy] = useState(false)
  const { data, loading, error, reload } = useFetch(() => notificationApi.list({ page, size: 20 }), [page])

  const markOne = async (n) => {
    if (n.read) return
    try { await notificationApi.markRead(n.id); reload(true) } catch (e) { toast.error(getErrorMessage(e)) }
  }
  const markAll = async () => {
    setBusy(true)
    try { const r = await notificationApi.markAllRead(); toast.success(`${r.markedRead} notification${r.markedRead === 1 ? '' : 's'} marked as read.`); reload(true) }
    catch (e) { toast.error(getErrorMessage(e)) } finally { setBusy(false) }
  }

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title="Notifications" actions={<Button variant="secondary" loading={busy} onClick={markAll}><CheckCheck className="h-4 w-4" />Mark all as read</Button>} />
      {loading ? <LoadingSpinner /> : error ? <ErrorMessage error={error} onRetry={reload} /> : !data.content.length ? (
        <EmptyState icon={Bell} title="You're all caught up" text="New activity on your jobs, applications and contracts will show up here." />
      ) : (
        <>
          <ul className="space-y-2">
            {data.content.map((n) => (
              <li key={n.id}>
                <button onClick={() => markOne(n)} className={`w-full rounded-2xl border p-4 text-left transition-colors ${n.read ? 'border-line bg-ink-900' : 'border-brand-400/40 bg-brand-500/10'}`} aria-label={`${n.title}${n.read ? '' : ' (unread, select to mark as read)'}`}>
                  <div className="flex items-start justify-between gap-3">
                    <div><p className="font-bold">{!n.read && <span className="mr-2 inline-block h-2 w-2 rounded-full bg-accent align-middle" />}{n.title}</p><p className="mt-0.5 text-sm text-slate-300">{n.message}</p></div>
                    <div className="shrink-0 text-right"><Badge value="SYSTEM">{n.type}</Badge><p className="mt-1 text-xs text-muted">{timeAgo(n.createdAt)}</p></div>
                  </div>
                </button>
              </li>
            ))}
          </ul>
          <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
