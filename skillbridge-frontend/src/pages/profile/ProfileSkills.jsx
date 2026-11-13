import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { profileApi } from '../../api/profile'
import { jobsApi } from '../../api/jobs'
import { useAuth } from '../../context/AuthContext'
import { useSave } from '../../hooks/useApi'
import Card, { CardBody } from '../../components/ui/Card'
import QueryBoundary from '../../components/ui/QueryBoundary'
import Select from '../../components/ui/Select'
import { Check } from 'lucide-react'

const arr = (d) => (Array.isArray(d) ? d : d?.content || [])
export default function ProfileSkills() {
  const { user } = useAuth()
  const [category, setCategory] = useState('')
  const mine = useQuery({ queryKey: ['profile', 'skills', user.id], queryFn: () => profileApi.list(user.id, 'skills') })
  const catalog = useQuery({ queryKey: ['skills', category], queryFn: () => jobsApi.skills(category) })
  const all = useQuery({ queryKey: ['skills', ''], queryFn: () => jobsApi.skills('') })
  const attach = useSave(profileApi.attachSkill, { invalidate: [['profile', 'skills']] })
  const detach = useSave(profileApi.detachSkill, { invalidate: [['profile', 'skills']] })
  const have = new Set(arr(mine.data).map((s) => s.id))
  const cats = [...new Set(arr(all.data).map((s) => s.category).filter(Boolean))]
  return (
    <Card><CardBody>
      <Select label="Category" value={category} onChange={(e) => setCategory(e.target.value)} className="mb-4 max-w-xs"><option value="">All categories</option>{cats.map((c) => <option key={c}>{c}</option>)}</Select>
      <QueryBoundary q={catalog}>{(d) => (
        <div className="flex flex-wrap gap-2">
          {arr(d).map((s) => { const on = have.has(s.id); return (
            <button key={s.id} disabled={attach.isPending || detach.isPending} aria-pressed={on} onClick={() => (on ? detach.mutate(s.id) : attach.mutate(s.id))}
              className={`inline-flex min-h-[36px] items-center gap-1 rounded border px-3 py-1 text-sm ${on ? 'border-primary bg-primary/15' : 'text-muted hover:text-fg'}`}>{on && <Check className="h-3.5 w-3.5" />}{s.name}</button>) })}
          {arr(d).length === 0 && <p className="text-sm text-muted">No skills in this category.</p>}
        </div>)}
      </QueryBoundary>
    </CardBody></Card>
  )
}
