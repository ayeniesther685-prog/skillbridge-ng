import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Send } from 'lucide-react'
import * as messageApi from '../api/messageApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { formatDateTime } from '../utils/dates.js'
import { Button, inputCls } from '../components/ui.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useToast } from '../components/Toast.jsx'

const PAGE_SIZE = 50

export default function ConversationDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const toast = useToast()
  const { data: conv, loading, error, reload } = useFetch(() => messageApi.getConversation(id), [id])
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [sending, setSending] = useState(false)
  const marked = useRef(new Set())
  const bottom = useRef(null)

  // Newest page: page 0 tells us totalPages; if more than one, fetch the last page.
  const loadMessages = useCallback(async () => {
    try {
      let page = await messageApi.messages(id, { page: 0, size: PAGE_SIZE, sort: 'createdAt,asc' })
      if (page.totalPages > 1) page = await messageApi.messages(id, { page: page.totalPages - 1, size: PAGE_SIZE, sort: 'createdAt,asc' })
      setMessages(page.content)
      const incoming = page.content.filter((m) => m.senderId !== user.id && !m.read && !marked.current.has(m.id))
      incoming.forEach((m) => marked.current.add(m.id))
      await Promise.allSettled(incoming.map((m) => messageApi.markRead(m.id)))
    } catch { /* keep current messages; next poll retries */ }
  }, [id, user.id])

  useEffect(() => {
    loadMessages()
    const t = setInterval(loadMessages, 8000)
    return () => clearInterval(t)
  }, [loadMessages])
  useEffect(() => { bottom.current?.scrollIntoView({ behavior: 'smooth' }) }, [messages.length])

  const send = async (e) => {
    e.preventDefault()
    const content = text.trim()
    if (!content || sending) return
    setSending(true)
    try { const m = await messageApi.send(id, content); setMessages((ms) => [...ms, m]); setText('') } catch (err) { toast.error(getErrorMessage(err)) } finally { setSending(false) }
  }

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorMessage error={error} onRetry={reload} />
  return (
    <div className="mx-auto flex h-[calc(100vh-9rem)] max-w-3xl flex-col rounded-2xl border border-line bg-ink-900">
      <div className="flex items-center gap-3 border-b border-line p-4">
        <Link to="/messages" aria-label="Back to conversations" className="grid h-9 w-9 place-items-center rounded-lg hover:bg-white/5"><ArrowLeft className="h-4 w-4" /></Link>
        <span className="grid h-10 w-10 place-items-center rounded-full bg-brand-500 font-bold">{conv.otherUserName?.[0]}</span>
        <div><Link to={`/freelancers?id=${conv.otherUserId}`} className="font-bold hover:text-brand-400">{conv.otherUserName}</Link><p className="text-xs text-muted">{conv.otherUserEmail}</p></div>
      </div>
      <div className="flex-1 space-y-3 overflow-y-auto p-4" aria-live="polite">
        {!messages.length && <p className="py-10 text-center text-sm text-muted">No messages yet. Say hello!</p>}
        {messages.map((m) => {
          const mine = m.senderId === user.id
          return (
            <div key={m.id} className={`flex ${mine ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[80%] rounded-2xl px-4 py-2.5 text-sm ${mine ? 'rounded-br-md bg-brand-500' : 'rounded-bl-md bg-ink-700'}`}>
                <p className="whitespace-pre-wrap break-words">{m.content}</p>
                <p className="mt-1 text-[10px] opacity-70">{formatDateTime(m.createdAt)}{mine && m.read ? ' · Read' : ''}</p>
              </div>
            </div>
          )
        })}
        <div ref={bottom} />
      </div>
      <form onSubmit={send} className="flex gap-2 border-t border-line p-3">
        <input className={inputCls} value={text} onChange={(e) => setText(e.target.value)} placeholder="Write a message…" aria-label="Message" maxLength={4000} />
        <Button type="submit" loading={sending} disabled={!text.trim()} aria-label="Send message"><Send className="h-4 w-4" /></Button>
      </form>
    </div>
  )
}
