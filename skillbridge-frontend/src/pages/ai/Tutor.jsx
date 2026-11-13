import { useEffect, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Send } from 'lucide-react'
import { aiApi } from '../../api/ai'
import { normalizeError } from '../../api/errors'
import Button from '../../components/ui/Button'
import Card from '../../components/ui/Card'
export default function Tutor() {
  const qc = useQueryClient()
  const [msgs, setMsgs] = useState([])
  const [text, setText] = useState('')
  const end = useRef(null)
  const quota = useQuery({ queryKey: ['ai', 'quota'], queryFn: aiApi.quota })
  const send = useMutation({
    mutationFn: aiApi.tutor,
    onSuccess: (r) => { setMsgs((m) => [...m, { role: 'ai', text: r.response }]); qc.invalidateQueries({ queryKey: ['ai'] }) },
    onError: (err) => {
      const e = normalizeError(err)
      const reset = e.fieldErrors?.resetAtDescription || e.fieldErrors?.resetAt
      setMsgs((m) => [...m, { role: 'error', text: e.status === 429 ? `You've used today's AI quota.${reset ? ` It resets ${reset}.` : ''}` : e.message }])
      if (e.status === 429) qc.invalidateQueries({ queryKey: ['ai', 'quota'] })
    },
  })
  useEffect(() => { end.current?.scrollIntoView({ block: 'end' }) }, [msgs, send.isPending])
  const remaining = send.data?.remainingToday ?? quota.data?.remainingToday ?? quota.data?.remaining
  const submit = (e) => { e.preventDefault(); const t = text.trim(); if (!t || send.isPending) return; setMsgs((m) => [...m, { role: 'user', text: t }]); setText(''); send.mutate(t) }
  return (
    <Card className="flex h-[65vh] flex-col">
      <div className="flex-1 space-y-3 overflow-y-auto p-4" aria-live="polite">
        {msgs.length === 0 && <p className="text-sm text-muted">Ask anything about your courses or skills. This chat isn’t saved after you leave the page.</p>}
        {msgs.map((m, i) => <div key={i} className={`max-w-[85%] whitespace-pre-line rounded-lg px-3 py-2 text-sm ${m.role === 'user' ? 'ml-auto bg-primary text-white' : m.role === 'error' ? 'border border-danger/40 text-danger' : 'bg-fg/10'}`}>{m.text}</div>)}
        {send.isPending && <div className="text-sm text-muted">Thinking…</div>}
        <div ref={end} />
      </div>
      <form onSubmit={submit} className="border-t p-3">
        <div className="flex gap-2"><input className="field" value={text} onChange={(e) => setText(e.target.value)} placeholder="Ask the tutor…" aria-label="Message" /><Button type="submit" loading={send.isPending} aria-label="Send"><Send className="h-4 w-4" /></Button></div>
        {remaining != null && <p className="mt-2 text-xs text-muted">{remaining} requests left today</p>}
      </form>
    </Card>
  )
}
