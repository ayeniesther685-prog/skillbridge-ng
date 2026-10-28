import { useMemo, useState } from 'react'
import * as skillApi from '../api/skillApi.js'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { Card, PageHeader, inputCls } from '../components/ui.jsx'
import SkillPill from '../components/SkillPill.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useToast } from '../components/Toast.jsx'

export default function Skills() {
  const toast = useToast()
  const catalog = useFetch(() => skillApi.list(), [])
  const mine = useFetch(() => skillApi.mine(), [])
  const [category, setCategory] = useState('')
  const [busyId, setBusyId] = useState(null)

  const mineIds = useMemo(() => new Set((mine.data ?? []).map((s) => s.id)), [mine.data])
  const categories = useMemo(() => [...new Set((catalog.data ?? []).map((s) => s.category).filter(Boolean))].sort(), [catalog.data])
  const grouped = useMemo(() => {
    const g = {}
    for (const s of catalog.data ?? []) if (!category || s.category === category) (g[s.category || 'Other'] ??= []).push(s)
    return g
  }, [catalog.data, category])

  const toggle = async (skill) => {
    if (busyId) return
    setBusyId(skill.id)
    try {
      const updated = mineIds.has(skill.id) ? await skillApi.detach(skill.id) : await skillApi.attach(skill.id)
      mine.setData(updated)
    } catch (e) { toast.error(getErrorMessage(e)) } finally { setBusyId(null) }
  }

  if (catalog.loading || mine.loading) return <LoadingSpinner />
  if (catalog.error || mine.error) return <ErrorMessage error={catalog.error ?? mine.error} onRetry={() => { catalog.reload(); mine.reload() }} />

  return (
    <div>
      <PageHeader title="My skills" subtitle="Pick from the SkillBridge catalog. Select a skill to add it; select it again to remove it." />
      <Card className="mb-6">
        <h2 className="mb-3 font-extrabold">Your skills ({mine.data.length})</h2>
        {mine.data.length ? <div className="flex flex-wrap gap-2">{mine.data.map((s) => <SkillPill key={s.id} skill={s} active onClick={() => toggle(s)} />)}</div> : <p className="text-sm text-muted">You haven't added any skills yet.</p>}
      </Card>
      <div className="mb-4 max-w-xs">
        <select className={inputCls} value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Filter by category">
          <option value="">All categories</option>
          {categories.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>
      <div className="space-y-5">
        {Object.entries(grouped).map(([cat, skills]) => (
          <Card key={cat}>
            <h3 className="mb-3 text-sm font-bold uppercase tracking-wide text-muted">{cat}</h3>
            <div className="flex flex-wrap gap-2">{skills.map((s) => <SkillPill key={s.id} skill={s} active={mineIds.has(s.id)} onClick={() => toggle(s)} />)}</div>
          </Card>
        ))}
      </div>
    </div>
  )
}
