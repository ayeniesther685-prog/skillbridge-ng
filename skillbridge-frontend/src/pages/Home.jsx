import { Link } from 'react-router-dom'
import { ArrowRight, Briefcase, FileSignature, MessageSquare, ShieldCheck, Sparkles, Users } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'

const features = [
  { icon: Users, title: 'Showcase your skills', text: 'Build a profile with skills, education, experience, certifications and a portfolio.' },
  { icon: Briefcase, title: 'Find real work', text: 'Browse and filter jobs, then apply with a proposal in minutes.' },
  { icon: FileSignature, title: 'Work with clarity', text: 'Contracts and milestones keep clients and freelancers aligned.' },
  { icon: MessageSquare, title: 'Talk directly', text: 'Built-in messaging and notifications keep every project moving.' },
  { icon: ShieldCheck, title: 'Build trust', text: 'Two-way reviews after completed work grow your reputation.' },
  { icon: Sparkles, title: 'More on the way', text: 'Payments, wallet and an AI career assistant are coming soon.' },
]

export default function Home() {
  const { isAuthenticated } = useAuth()
  return (
    <div className="space-y-16">
      <section className="relative overflow-hidden rounded-3xl border border-line bg-ink-900 px-6 py-16 text-center sm:px-12 sm:py-24">
        <div className="grid-bg absolute inset-0" aria-hidden="true" />
        <div className="absolute left-1/2 top-0 h-64 w-96 -translate-x-1/2 rounded-full bg-brand-500/25 blur-3xl" aria-hidden="true" />
        <div className="relative mx-auto max-w-3xl">
          <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl">Connect skills to <span className="text-brand-400">opportunity.</span></h1>
          <p className="mx-auto mt-5 max-w-xl text-lg text-muted">Discover jobs, showcase your skills and connect with people who need what you can do.</p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Link to="/jobs" className="inline-flex items-center gap-2 rounded-xl bg-brand-500 px-6 py-3 text-sm font-bold hover:bg-brand-600">Browse jobs <ArrowRight className="h-4 w-4" /></Link>
            <Link to={isAuthenticated ? '/dashboard' : '/register'} className="rounded-xl border border-line bg-ink-800 px-6 py-3 text-sm font-bold hover:bg-ink-700">{isAuthenticated ? 'Go to dashboard' : 'Create free account'}</Link>
          </div>
        </div>
      </section>
      <section aria-label="Features" className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {features.map((f) => (
          <div key={f.title} className="rounded-2xl border border-line bg-ink-900 p-6">
            <span className="grid h-10 w-10 place-items-center rounded-xl bg-brand-500/15 text-brand-400"><f.icon className="h-5 w-5" /></span>
            <h3 className="mt-4 font-extrabold">{f.title}</h3>
            <p className="mt-1 text-sm text-muted">{f.text}</p>
          </div>
        ))}
      </section>
    </div>
  )
}
