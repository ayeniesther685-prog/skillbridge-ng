import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Search } from 'lucide-react'
import * as jobApi from '../api/jobApi.js'
import * as skillApi from '../api/skillApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { Button, Card, Field, PageHeader, inputCls } from '../components/ui.jsx'
import { JOB_TYPES, LEVELS } from '../components/JobForm.jsx'
import JobCard from '../components/JobCard.jsx'
import SkillPill from '../components/SkillPill.jsx'
import Pagination from '../components/Pagination.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import EmptyState from '../components/EmptyState.jsx'

const STATUSES = ['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'CLOSED']
const initial = { search: '', location: '', jobType: '', experienceLevel: '', status: '', minBudget: '', maxBudget: '', skills: [], sort: 'createdAt,desc' }

export default function Jobs() {
  const { isAuthenticated } = useAuth()
  const [draft, setDraft] = useState(initial)
  const [applied, setApplied] = useState(initial)
  const [page, setPage] = useState(0)
  const { data: catalog } = useFetch(() => skillApi.list(), [])

  const params = useMemo(() => {
    const p = { page, size: 12, sort: applied.sort }
    for (const k of ['search', 'location', 'jobType', 'experienceLevel', 'status', 'minBudget', 'maxBudget']) if (applied[k] !== '') p[k] = applied[k]
    if (applied.skills.length) p.skill = applied.skills
    return p
  }, [applied, page])
  const { data, loading, error, reload } = useFetch(() => jobApi.list(params), [params])

  const set = (k) => (e) => setDraft((d) => ({ ...d, [k]: e.target.value }))
  const toggleSkill = (id) => setDraft((d) => ({ ...d, skills: d.skills.includes(id) ? d.skills.filter((x) => x !== id) : [...d.skills, id] }))
  const apply = (e) => { e.preventDefault(); setPage(0); setApplied(draft) }
  const reset = () => { setDraft(initial); setApplied(initial); setPage(0) }

  return (
    <div>
      <PageHeader title="Find jobs" subtitle={data ? `${data.totalElements} job${data.totalElements === 1 ? '' : 's'} found` : 'Search and filter opportunities.'}
        actions={isAuthenticated && <Link to="/jobs/new"><Button><Plus className="h-4 w-4" />Post a job</Button></Link>} />
      <div className="grid gap-6 lg:grid-cols-[18rem_1fr]">
        <Card className="h-fit">
          <form onSubmit={apply} className="space-y-4">
            <Field label="Search"><div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" /><input className={`${inputCls} pl-9`} value={draft.search} onChange={set('search')} placeholder="Title or keyword" /></div></Field>
            <Field label="Location"><input className={inputCls} value={draft.location} onChange={set('location')} placeholder="Lagos" /></Field>
            <Field label="Job type"><select className={inputCls} value={draft.jobType} onChange={set('jobType')}><option value="">Any</option>{JOB_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}</select></Field>
            <Field label="Experience"><select className={inputCls} value={draft.experienceLevel} onChange={set('experienceLevel')}><option value="">Any</option>{LEVELS.map((t) => <option key={t}>{t}</option>)}</select></Field>
            <Field label="Status"><select className={inputCls} value={draft.status} onChange={set('status')}><option value="">Any</option>{STATUSES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}</select></Field>
            <div className="grid grid-cols-2 gap-2">
              <Field label="Min ₦"><input className={inputCls} type="number" min="0" value={draft.minBudget} onChange={set('minBudget')} /></Field>
              <Field label="Max ₦"><input className={inputCls} type="number" min="0" value={draft.maxBudget} onChange={set('maxBudget')} /></Field>
            </div>
            <Field label="Sort"><select className={inputCls} value={draft.sort} onChange={set('sort')}><option value="createdAt,desc">Newest first</option><option value="createdAt,asc">Oldest first</option></select></Field>
            <div>
              <p className="mb-2 text-sm font-semibold text-slate-200">Skills</p>
              <div className="flex max-h-40 flex-wrap gap-1.5 overflow-y-auto">{catalog?.map((s) => <SkillPill key={s.id} skill={s} active={draft.skills.includes(s.id)} onClick={() => toggleSkill(s.id)} />)}</div>
            </div>
            <div className="flex gap-2"><Button type="submit" className="flex-1">Apply filters</Button><Button type="button" variant="secondary" onClick={reset}>Reset</Button></div>
          </form>
        </Card>
        <div>
          {loading ? <LoadingSpinner /> : error ? <ErrorMessage error={error} onRetry={reload} /> : !data.content.length ? (
            <EmptyState title="No jobs match your filters" text="Try widening your search or clearing some filters." action={<Button variant="secondary" onClick={reset}>Clear filters</Button>} />
          ) : (
            <>
              <div className="grid gap-4 md:grid-cols-2">{data.content.map((j) => <JobCard key={j.id} job={j} />)}</div>
              <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
            </>
          )}
        </div>
      </div>
    </div>
  )
}
