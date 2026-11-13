import { useEffect, useState } from 'react'
import { Search } from 'lucide-react'
export default function SearchInput({ value, onChange, placeholder = 'Search', delay = 350 }) {
  const [local, setLocal] = useState(value || '')
  useEffect(() => { const t = setTimeout(() => local !== value && onChange(local), delay); return () => clearTimeout(t) }, [local]) // eslint-disable-line
  return (
    <div className="relative">
      <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
      <input className="field pl-9" value={local} onChange={(e) => setLocal(e.target.value)} placeholder={placeholder} aria-label={placeholder} />
    </div>
  )
}
