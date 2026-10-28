import { ChevronLeft, ChevronRight } from 'lucide-react'

// page is 0-based (Spring Page). Pass the Spring page object fields.
export default function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null
  return (
    <nav aria-label="Pagination" className="mt-6 flex items-center justify-center gap-3">
      <button onClick={() => onChange(page - 1)} disabled={page <= 0} aria-label="Previous page" className="grid h-10 w-10 place-items-center rounded-xl border border-line bg-ink-900 disabled:opacity-40"><ChevronLeft className="h-4 w-4" /></button>
      <span className="text-sm text-muted">Page <b className="text-white">{page + 1}</b> of {totalPages}</span>
      <button onClick={() => onChange(page + 1)} disabled={page >= totalPages - 1} aria-label="Next page" className="grid h-10 w-10 place-items-center rounded-xl border border-line bg-ink-900 disabled:opacity-40"><ChevronRight className="h-4 w-4" /></button>
    </nav>
  )
}
