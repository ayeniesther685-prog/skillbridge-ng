import { AlertCircle } from 'lucide-react'
import Button from './Button'
import { normalizeError } from '../../api/errors'
export default function ErrorState({ error, onRetry }) {
  const e = normalizeError(error)
  return (
    <div className="flex flex-col items-center rounded-lg border border-danger/30 px-6 py-10 text-center">
      <AlertCircle className="mb-2 h-7 w-7 text-danger" />
      <p className="text-sm">{e.message}</p>
      {onRetry && <Button variant="outline" size="sm" className="mt-4" onClick={onRetry}>Try again</Button>}
    </div>
  )
}
