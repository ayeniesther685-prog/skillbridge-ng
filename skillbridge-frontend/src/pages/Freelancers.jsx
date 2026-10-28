import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Search } from 'lucide-react'
import { Button, Card, PageHeader, inputCls } from '../components/ui.jsx'
import ProfileView from '../components/ProfileView.jsx'
import { useAuth } from '../context/AuthContext.jsx'

// The backend exposes public profile data per user id but has no endpoint that lists users,
// so people are opened by id (links from jobs, applications and conversations use ?id=).
export default function Freelancers() {
  const [params, setParams] = useSearchParams()
  const { user } = useAuth()
  const id = Number(params.get('id'))
  const [input, setInput] = useState(params.get('id') ?? '')
  const valid = Number.isInteger(id) && id > 0

  const submit = (e) => {
    e.preventDefault()
    const n = Number(input)
    if (Number.isInteger(n) && n > 0) setParams({ id: String(n) })
  }

  return (
    <div>
      <PageHeader title="People" subtitle="Look up a freelancer or client's public profile by user ID." />
      <Card className="mb-6">
        <form onSubmit={submit} className="flex flex-wrap items-end gap-3">
          <label className="min-w-40 flex-1"><span className="mb-1.5 block text-sm font-semibold">User ID</span>
            <input className={inputCls} inputMode="numeric" value={input} onChange={(e) => setInput(e.target.value)} placeholder="e.g. 22" /></label>
          <Button type="submit"><Search className="h-4 w-4" />View profile</Button>
        </form>
        <p className="mt-3 text-xs text-muted">Tip: you'll find user IDs on job pages, applications and conversations — those pages link straight to profiles.</p>
      </Card>
      {valid ? <ProfileView userId={id} basic={user?.id === id ? user : undefined} /> : null}
    </div>
  )
}
