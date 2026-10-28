import { Link } from 'react-router-dom'
import { Bell, Briefcase, FileSignature, FileText, MessageSquare } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import * as applicationApi from '../api/applicationApi.js'
import * as contractApi from '../api/contractApi.js'
import * as notificationApi from '../api/notificationApi.js'
import * as messageApi from '../api/messageApi.js'
import * as profileApi from '../api/profileApi.js'
import * as skillApi from '../api/skillApi.js'
import { educationApi } from '../api/educationApi.js'
import { experienceApi } from '../api/experienceApi.js'
import { portfolioApi } from '../api/portfolioApi.js'
import { Card, PageHeader } from '../components/ui.jsx'
import ComingSoon from '../components/ComingSoon.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'

async function loadStats(user) {
  const ok = (r, d) => (r.status === 'fulfilled' ? r.value : d)
  const [apps, contracts, unread, convs, profile, skills, edu, exp, port] = await Promise.allSettled([
    applicationApi.mine({ size: 1 }), contractApi.list({ size: 1 }), notificationApi.unreadCount(),
    messageApi.conversations({ size: 100 }), profileApi.getMyProfile(), skillApi.mine(),
    educationApi.list(user.id), experienceApi.list(user.id), portfolioApi.list(user.id),
  ])
  const checks = [
    ['Add a headline', !!ok(profile, null)?.headline, '/profile/edit'],
    ['Add a bio', !!user.bio, null],
    ['Add at least one skill', ok(skills, []).length > 0, '/skills'],
    ['Add education', ok(edu, []).length > 0, '/education'],
    ['Add work experience', ok(exp, []).length > 0, '/experience'],
    ['Add a portfolio item', ok(port, []).length > 0, '/portfolio'],
  ]
  return {
    applications: ok(apps, { totalElements: 0 }).totalElements,
    contracts: ok(contracts, { totalElements: 0 }).totalElements,
    unreadNotifications: ok(unread, { count: 0 }).count,
    unreadMessages: ok(convs, { content: [] }).content.reduce((n, c) => n + (c.unreadCount ?? 0), 0),
    checks,
    completion: Math.round((checks.filter((c) => c[1]).length / checks.length) * 100),
  }
}

export default function Dashboard() {
  const { user } = useAuth()
  const { data, loading } = useFetch(() => loadStats(user), [user.id])
  if (loading || !data) return <LoadingSpinner />

  const stats = [
    { label: 'Applications', value: data.applications, icon: FileText, to: '/applications' },
    { label: 'Contracts', value: data.contracts, icon: FileSignature, to: '/contracts' },
    { label: 'Unread notifications', value: data.unreadNotifications, icon: Bell, to: '/notifications' },
    { label: 'Unread messages', value: data.unreadMessages, icon: MessageSquare, to: '/messages' },
  ]
  return (
    <div>
      <PageHeader title={`Welcome back, ${user.firstName}`} subtitle="Here's what's happening on SkillBridge."
        actions={<Link to="/jobs/new" className="inline-flex h-11 items-center gap-2 rounded-xl bg-brand-500 px-4 text-sm font-bold hover:bg-brand-600"><Briefcase className="h-4 w-4" />Post a job</Link>} />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((s) => (
          <Link key={s.label} to={s.to}>
            <Card className="transition-colors hover:border-brand-400/60">
              <s.icon className="h-5 w-5 text-brand-400" aria-hidden="true" />
              <p className="mt-3 text-3xl font-extrabold">{s.value}</p>
              <p className="text-sm text-muted">{s.label}</p>
            </Card>
          </Link>
        ))}
      </div>

      <Card className="mt-6">
        <div className="flex items-center justify-between">
          <h2 className="font-extrabold">Profile completion</h2>
          <span className="text-lg font-extrabold text-brand-400">{data.completion}%</span>
        </div>
        <div className="mt-3 h-2.5 overflow-hidden rounded-full bg-ink-700" role="progressbar" aria-valuenow={data.completion} aria-valuemin={0} aria-valuemax={100}>
          <div className="h-full rounded-full bg-brand-500 transition-all" style={{ width: `${data.completion}%` }} />
        </div>
        <ul className="mt-4 grid gap-2 text-sm sm:grid-cols-2">
          {data.checks.map(([text, done, to]) => (
            <li key={text} className="flex items-center gap-2">
              <span className={`grid h-5 w-5 place-items-center rounded-full text-[11px] font-bold ${done ? 'bg-emerald-500/20 text-emerald-300' : 'bg-ink-700 text-slate-400'}`}>{done ? '✓' : '·'}</span>
              {done || !to ? <span className={done ? 'text-slate-300' : 'text-muted'}>{text}</span> : <Link to={to} className="font-semibold text-brand-400 hover:underline">{text}</Link>}
            </li>
          ))}
        </ul>
        <p className="mt-3 text-xs text-muted">Bio can't be edited yet — the backend has no endpoint for basic account info.</p>
      </Card>

      <div className="mt-6 grid gap-4 md:grid-cols-3">
        <ComingSoon title="Payments" phase="Phase 6" description="Secure Paystack payments for milestones." />
        <ComingSoon title="Wallet" phase="Phase 6" description="Balance, transactions and withdrawals." />
        <ComingSoon title="AI Assistant" phase="Phase 7" description="Career guidance, CV analysis and job matching." />
      </div>
    </div>
  )
}
