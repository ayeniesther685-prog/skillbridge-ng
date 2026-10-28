import { Inbox } from 'lucide-react'

export default function EmptyState({ icon: Icon = Inbox, title, text, action }) {
  return (
    <div className="grid place-items-center rounded-2xl border border-dashed border-line px-6 py-14 text-center">
      <Icon className="h-9 w-9 text-slate-500" aria-hidden="true" />
      <h3 className="mt-3 text-base font-bold">{title}</h3>
      {text && <p className="mt-1 max-w-sm text-sm text-muted">{text}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
