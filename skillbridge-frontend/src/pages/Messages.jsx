import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { MessageSquare } from 'lucide-react'
import * as messageApi from '../api/messageApi.js'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { timeAgo } from '../utils/dates.js'
import { Button, Card, PageHeader, inputCls } from '../components/ui.jsx'
import Pagination from '../components/Pagination.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { useToast } from '../components/Toast.jsx'

export default function Messages() {
  const navigate = useNavigate()
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [otherId, setOtherId] = useState('')
  const [starting, setStarting] = useState(false)
  const { data, loading, error, reload } = useFetch(() => messageApi.conversations({ page, size: 20 }), [page])

  const start = async (e) => {
    e.preventDefault()
    const n = Number(otherId)
    if (!Number.isInteger(n) || n <= 0) return toast.error('Enter a valid user ID.')
    setStarting(true)
    try { const c = await messageApi.startConversation(n); navigate(`/messages/${c.id}`) } catch (err) { toast.error(getErrorMessage(err)); setStarting(false) }
  }

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title="Messages" subtitle="Your conversations." />
      <Card className="mb-5">
        <form onSubmit={start} className="flex flex-wrap items-end gap-3">
          <label className="min-w-40 flex-1"><span className="mb-1.5 block text-sm font-semibold">Start a conversation</span>
            <input className={inputCls} inputMode="numeric" placeholder="User ID (shown on profiles and applications)" value={otherId} onChange={(e) => setOtherId(e.target.value)} /></label>
          <Button type="submit" loading={starting}>Start</Button>
        </form>
      </Card>
      {loading ? <LoadingSpinner /> : error ? <ErrorMessage error={error} onRetry={reload} /> : !data.content.length ? (
        <EmptyState icon={MessageSquare} title="No conversations yet" text="Message a client from a job page, or a freelancer from their profile." />
      ) : (
        <>
          <ul className="space-y-2">
            {data.content.map((c) => (
              <li key={c.id}>
                <Link to={`/messages/${c.id}`} className="flex items-center gap-3 rounded-2xl border border-line bg-ink-900 p-4 transition-colors hover:border-brand-400/60">
                  <span className="grid h-11 w-11 shrink-0 place-items-center rounded-full bg-brand-500 font-bold">{c.otherUserName?.[0]}</span>
                  <div className="min-w-0 flex-1"><p className="truncate font-bold">{c.otherUserName}</p><p className="truncate text-xs text-muted">{c.otherUserEmail}</p></div>
                  <div className="text-right"><p className="text-xs text-muted">{c.lastMessageAt ? timeAgo(c.lastMessageAt) : 'New'}</p>
                    {c.unreadCount > 0 && <span className="mt-1 inline-block rounded-full bg-accent px-2 py-0.5 text-xs font-bold text-black">{c.unreadCount}</span>}</div>
                </Link>
              </li>
            ))}
          </ul>
          <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
