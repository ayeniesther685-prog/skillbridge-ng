import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LogOut } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import { label } from '../utils/dates.js'
import { Button, Card, PageHeader } from '../components/ui.jsx'
import ComingSoon from '../components/ComingSoon.jsx'

const tabs = ['Account', 'Payments', 'AI Assistant']

export default function Settings() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [tab, setTab] = useState('Account')
  const [busy, setBusy] = useState(false)
  const handleLogout = async () => { setBusy(true); await logout(); navigate('/login', { replace: true }) }

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title="Settings" />
      <div role="tablist" className="mb-5 flex gap-1 rounded-xl border border-line bg-ink-900 p-1">
        {tabs.map((t) => <button key={t} role="tab" aria-selected={tab === t} onClick={() => setTab(t)} className={`flex-1 rounded-lg px-3 py-2 text-sm font-bold transition-colors ${tab === t ? 'bg-brand-500' : 'text-slate-300 hover:bg-white/5'}`}>{t}</button>)}
      </div>
      {tab === 'Account' && (
        <Card className="space-y-4">
          <dl className="grid gap-3 text-sm sm:grid-cols-2">
            {[['Name', `${user.firstName} ${user.lastName}`], ['Email', user.email], ['Phone', user.phone || '—'], ['Location', user.location || '—'], ['Roles', user.roles?.map(label).join(', ')], ['User ID', user.id]].map(([k, v]) => (
              <div key={k}><dt className="text-muted">{k}</dt><dd className="font-semibold">{v}</dd></div>
            ))}
          </dl>
          <p className="text-xs text-muted">Account details are read-only for now.</p>
          <Button variant="danger" loading={busy} onClick={handleLogout}><LogOut className="h-4 w-4" />Log out</Button>
        </Card>
      )}
      {tab === 'Payments' && <ComingSoon title="Payments & wallet" phase="Phase 6" description="Paystack payments, wallet balance and transaction history." buttonLabel="Add payment method" />}
      {tab === 'AI Assistant' && <ComingSoon title="AI career assistant" phase="Phase 7" description="CV analyzer, job matching and proposal generator." buttonLabel="Enable assistant" />}
    </div>
  )
}
