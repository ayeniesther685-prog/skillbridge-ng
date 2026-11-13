import { Loader2 } from 'lucide-react'
export default function Spinner({ full }) {
  const el = <Loader2 className="h-6 w-6 animate-spin text-primary" aria-label="Loading" />
  return full ? <div className="flex h-screen items-center justify-center bg-bg">{el}</div> : el
}
