import { Loader2 } from 'lucide-react'

export default function LoadingSpinner({ fullScreen = false, label = 'Loading…' }) {
  return (
    <div className={`grid place-items-center ${fullScreen ? 'min-h-screen bg-ink-950' : 'py-16'}`} role="status" aria-live="polite">
      <Loader2 className="h-7 w-7 animate-spin text-brand-400" aria-hidden="true" />
      <span className="sr-only">{label}</span>
    </div>
  )
}
