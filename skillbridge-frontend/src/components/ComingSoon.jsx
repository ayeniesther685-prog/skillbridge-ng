import { Lock } from 'lucide-react'

// Placeholder for features whose backend endpoints do not exist yet. Never simulates an operation.
export default function ComingSoon({ title, description, phase, buttonLabel = 'Coming soon' }) {
  return (
    <div className="rounded-2xl border border-line bg-ink-900 p-5">
      <div className="flex items-center gap-2 text-brand-400"><Lock className="h-4 w-4" aria-hidden="true" /><span className="text-xs font-bold uppercase tracking-wide">{phase ?? 'Coming soon'}</span></div>
      <h3 className="mt-2 text-base font-extrabold">{title}</h3>
      <p className="mt-1 text-sm text-muted">{description}</p>
      <button disabled className="mt-4 h-10 cursor-not-allowed rounded-xl border border-line bg-ink-800 px-4 text-sm font-bold text-slate-500">{buttonLabel}</button>
    </div>
  )
}
