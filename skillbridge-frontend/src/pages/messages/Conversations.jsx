import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { MessageSquare, Plus } from 'lucide-react'
import PageHeader from '../../components/ui/PageHeader'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Dialog from '../../components/ui/Dialog'
import Pagination from '../../components/ui/Pagination'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { messagesApi } from '../../api/messages'
import { useAuth } from '../../context/AuthContext'
import { usePaged, useSave } from '../../hooks/useApi'
import { nameOf } from '../../utils/ids'
import { fmtDate } from '../../utils/format'
export const otherPerson = (c, me) => c.otherUser || c.participants?.find((p) => p.id !== me) || (c.user1?.id === me ? c.user2 : c.user1) || c.participant
export default function Conversations() {
  const nav = useNavigate()
  const { user } = useAuth()
  const [open, setOpen] = useState(false); const [uid, setUid] = useState('')
  const { q, setPage } = usePaged(['conversations'], messagesApi.conversations)
  const start = useSave(messagesApi.start, { invalidate: [['conversations']], onSuccess: (c) => { setOpen(false); nav(`/messages/${c.id}`) } })
  return (
    <div>
      <PageHeader title="Messages" actions={<Button size="sm" onClick={() => setOpen(true)}><Plus className="h-4 w-4" />New conversation</Button>} />
      <QueryBoundary q={q}>{(d) => d.content.length === 0 ? <EmptyState icon={MessageSquare} title="No conversations yet" description="Start one with a user ID from their public profile page." action={<Button size="sm" onClick={() => setOpen(true)}>New conversation</Button>} /> :
        <><div className="space-y-2">{d.content.map((c) => (
          <Card key={c.id} className="cursor-pointer p-4 hover:border-primary/60" onClick={() => nav(`/messages/${c.id}`)}>
            <div className="flex items-center justify-between gap-3 text-sm"><span className="font-medium">{nameOf(otherPerson(c, user.id)) || `Conversation #${c.id}`}</span><span className="text-xs text-muted">{fmtDate(c.lastMessageAt || c.updatedAt)}</span></div>
            {c.lastMessage && <p className="mt-1 line-clamp-1 text-sm text-muted">{typeof c.lastMessage === 'string' ? c.lastMessage : c.lastMessage.content}</p>}
          </Card>))}</div><Pagination page={d} onPage={setPage} /></>}</QueryBoundary>
      <Dialog open={open} onClose={() => setOpen(false)} title="New conversation">
        <div className="space-y-4"><Input label="User ID" type="number" value={uid} onChange={(e) => setUid(e.target.value)} hint="Find a user's ID on their public profile page." />
          <div className="flex justify-end gap-2"><Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button><Button disabled={!uid} loading={start.isPending} onClick={() => start.mutate(Number(uid))}>Start</Button></div></div>
      </Dialog>
    </div>
  )
}
