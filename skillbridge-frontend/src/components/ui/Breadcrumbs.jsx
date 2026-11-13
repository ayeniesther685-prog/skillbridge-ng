import { Link } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
export default function Breadcrumbs({ items }) {
  return (
    <nav aria-label="Breadcrumb" className="mb-3 flex flex-wrap items-center gap-1 text-sm text-muted">
      {items.map(([label, to], i) => (
        <span key={label} className="flex items-center gap-1">
          {i > 0 && <ChevronRight className="h-3.5 w-3.5" />}
          {to ? <Link to={to} className="hover:text-fg">{label}</Link> : <span className="text-fg">{label}</span>}
        </span>
      ))}
    </nav>
  )
}
