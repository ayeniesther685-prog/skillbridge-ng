import { AlertCircle } from 'lucide-react'
import { getErrorMessage } from '../utils/errors.js'

export default function ErrorMessage({ error, message, onRetry }) {
  const text = message ?? getErrorMessage(error)
  return (
    <div role="alert" className="flex items-start gap-3 rounded-xl border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-200">
      <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
      <div className="flex-1 whitespace-pre-line">{text}</div>
      {onRetry && <button onClick={() => onRetry()} className="font-bold underline">Retry</button>}
    </div>
  )
}
