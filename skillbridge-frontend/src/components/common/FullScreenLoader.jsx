import { Loader2 } from 'lucide-react'

export default function FullScreenLoader() {
  return (
    <div className="grid min-h-screen place-items-center bg-ink-950" role="status" aria-live="polite">
      <Loader2 className="h-8 w-8 animate-spin text-brand-400" aria-hidden="true" />
      <span className="sr-only">Loading…</span>
    </div>
  )
}
