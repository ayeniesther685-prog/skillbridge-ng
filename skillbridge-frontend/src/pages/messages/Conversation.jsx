import { useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { Send } from 'lucide-react'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Button from '../../components/ui/Button'
import Card from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { messagesApi } from '../../api/messages'
import { useAuth } from '../../context/AuthContext'
import { useSave } from '../../hooks/useApi'
import { nameOf } from '../../utils/ids'
import { otherPerson } from './Conversations'
export default function Conversation() {
  const { id } = useParams()
  const { user } = useAuth()
  const [text, setText] = useState('')
  const end = useRef(null)
  const conv = useQuery({ queryKey: ['conversation', id], queryFn: () => messagesApi.get(id) })
  const q = useQuery({ queryKey: ['messages', id], queryFn: () => messagesApi.messages(id, { page: 0, size: 100 }), refetchInterval: 10000, placeholderData: keepPreviousData })
  const send = useSave(messagesApi.send, { invalidate: [['messages', id], ['conversations']], onSuccess: () => setText('') })
  const msgs = [...(q.data?.content || [])].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
  const sender = (m) => m.senderId ?? m.sender?.id
  useEffect(() => {
    msgs.filter((m) => sender(m) !== user.id && !(m.read ?? m.isRead ?? m.readAt)).forEach((m) => messagesApi.markRead(m.id).catch(() => {}))
    end.current?.scrollIntoView({ block: 'end' })
  }, [q.data]) // eslint-disable-line
  return (
    <div>
      <Breadcrumbs items={[['Messages', '/messages'], [nameOf(conv.data && otherPerson(conv.data, user.id)) || `Conversation #${id}`]]} />
      <Card className="flex h-[70vh] flex-col">
        <div className="flex-1 space-y-2 overflow-y-auto p-4" aria-live="polite">
          <QueryBoundary q={q}>{() => msgs.length === 0 ? <EmptyState title="No messages yet" description="Say hello." /> :
            msgs.map((m) => <div key={m.id} className={`max-w-[80%] whitespace-pre-line rounded-lg px-3 py-2 text-sm ${sender(m) === user.id ? 'ml-auto bg-primary text-white' : 'bg-fg/10'}`}>{m.content}</div>)}</QueryBoundary>
          <div ref={end} />
        </div>
        <form className="flex gap-2 border-t p-3" onSubmit={(e) => { e.preventDefault(); if (text.trim()) send.mutate({ id, content: text.trim() }) }}>
          <input className="field" value={text} onChange={(e) => setText(e.target.value)} placeholder="Write a message…" aria-label="Message" maxLength={2000} />
          <Button type="submit" loading={send.isPending} aria-label="Send"><Send className="h-4 w-4" /></Button>
        </form>
      </Card>
    </div>
  )
}
