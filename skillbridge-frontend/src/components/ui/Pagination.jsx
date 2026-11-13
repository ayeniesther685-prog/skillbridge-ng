import { ChevronLeft, ChevronRight } from 'lucide-react'
import Button from './Button'
export default function Pagination({ page, onPage }) {
  if (!page || page.totalPages <= 1) return null
  return (
    <div className="mt-6 flex items-center justify-between text-sm">
      <span className="text-muted">Page {page.number + 1} of {page.totalPages} · {page.totalElements} results</span>
      <div className="flex gap-2">
        <Button variant="outline" size="sm" disabled={page.first} onClick={() => onPage(page.number - 1)}><ChevronLeft className="h-4 w-4" />Previous</Button>
        <Button variant="outline" size="sm" disabled={page.last} onClick={() => onPage(page.number + 1)}>Next<ChevronRight className="h-4 w-4" /></Button>
      </div>
    </div>
  )
}
