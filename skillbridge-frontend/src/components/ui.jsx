import { useEffect } from 'react'
import { Loader2, X } from 'lucide-react'

export const inputCls =
  'h-11 w-full rounded-xl border border-line bg-ink-800 px-3.5 text-sm text-white placeholder:text-slate-500 transition-colors focus:border-brand-400 focus:outline-none focus:ring-2 focus:ring-brand-400/30 disabled:opacity-60'
export const textareaCls = inputCls.replace('h-11', 'min-h-28 py-2.5')

export const Card = ({ className = '', ...p }) => (
  <div className={`rounded-2xl border border-line bg-ink-900 p-5 shadow-lg shadow-black/20 ${className}`} {...p} />
)

const variants = {
  primary: 'bg-brand-500 text-white hover:bg-brand-600 shadow-lg shadow-brand-500/20',
  secondary: 'border border-line bg-ink-800 text-slate-200 hover:bg-ink-700',
  danger: 'bg-red-500/15 text-red-300 border border-red-500/30 hover:bg-red-500/25',
  ghost: 'text-slate-300 hover:bg-white/5',
}
export function Button({ variant = 'primary', loading = false, size = 'md', className = '', children, disabled, ...p }) {
  const sz = size === 'sm' ? 'h-9 px-3 text-xs' : 'h-11 px-4 text-sm'
  return (
    <button
      disabled={disabled || loading}
      aria-busy={loading}
      className={`inline-flex items-center justify-center gap-2 rounded-xl font-bold transition-colors active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50 ${sz} ${variants[variant]} ${className}`}
      {...p}
    >
      {loading && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
      {children}
    </button>
  )
}

export const Field = ({ label, hint, className = '', children }) => (
  <label className={`block ${className}`}>
    <span className="mb-1.5 block text-sm font-semibold text-slate-200">{label}</span>
    {children}
    {hint && <span className="mt-1 block text-xs text-muted">{hint}</span>}
  </label>
)

const badgeColors = {
  OPEN: 'emerald', ACTIVE: 'emerald', ACCEPTED: 'emerald', APPROVED: 'emerald', COMPLETED: 'blue',
  PENDING: 'amber', SHORTLISTED: 'indigo', IN_PROGRESS: 'indigo', DRAFT: 'slate',
  REJECTED: 'red', CANCELLED: 'red', WITHDRAWN: 'slate', CLOSED: 'slate', DISPUTED: 'red',
}
const tone = {
  emerald: 'bg-emerald-500/15 text-emerald-300', blue: 'bg-blue-500/15 text-blue-300', amber: 'bg-amber-500/15 text-amber-300',
  indigo: 'bg-indigo-500/20 text-indigo-300', red: 'bg-red-500/15 text-red-300', slate: 'bg-slate-500/20 text-slate-300',
}
export const Badge = ({ value, children }) => (
  <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${tone[badgeColors[value] ?? 'slate']}`}>
    {children ?? value?.replaceAll('_', ' ')}
  </span>
)

export const PageHeader = ({ title, subtitle, actions }) => (
  <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
    <div>
      <h1 className="text-2xl font-extrabold tracking-tight sm:text-3xl">{title}</h1>
      {subtitle && <p className="mt-1 text-sm text-muted">{subtitle}</p>}
    </div>
    {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
  </div>
)

export function Modal({ open, title, onClose, children }) {
  useEffect(() => {
    if (!open) return
    const onKey = (e) => e.key === 'Escape' && onClose()
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onClose])
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/70 p-0 sm:items-center sm:p-4" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div role="dialog" aria-modal="true" aria-label={title} className="max-h-[92vh] w-full max-w-lg overflow-y-auto rounded-t-3xl border border-line bg-ink-900 p-6 sm:rounded-3xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-extrabold">{title}</h2>
          <button onClick={onClose} aria-label="Close" className="grid h-9 w-9 place-items-center rounded-lg text-muted hover:bg-white/5"><X className="h-4 w-4" /></button>
        </div>
        {children}
      </div>
    </div>
  )
}
