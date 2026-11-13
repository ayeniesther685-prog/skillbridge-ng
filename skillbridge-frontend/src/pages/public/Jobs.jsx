import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { Briefcase, MapPin } from 'lucide-react'
import { jobsApi } from '../../api/jobs'
import PageHeader from '../../components/ui/PageHeader'
import SearchInput from '../../components/ui/SearchInput'
import Select from '../../components/ui/Select'
import Card from '../../components/ui/Card'
import Badge from '../../components/ui/Badge'
import StatusBadge from '../../components/ui/StatusBadge'
import Pagination from '../../components/ui/Pagination'
import EmptyState from '../../components/ui/EmptyState'
import ErrorState from '../../components/ui/ErrorState'
import { SkeletonList } from '../../components/ui/Skeleton'
import { label, fmtDate } from '../../utils/format'
import { jobBudget } from './jobUtils'

export default function Jobs() {
  const [sp, setSp] = useSearchParams()
  const f = { search: sp.get('search') || '', jobType: sp.get('jobType') || '', experienceLevel: sp.get('experienceLevel') || '', location: sp.get('location') || '', page: Number(sp.get('page') || 0) }
  const set = (patch) => { const n = new URLSearchParams(sp); Object.entries({ page: 0, ...patch }).forEach(([k, v]) => (v === '' || v == null ? n.delete(k) : n.set(k, v))); setSp(n) }
  const q = useQuery({ queryKey: ['jobs', f], queryFn: () => jobsApi.list({ ...f, size: 10 }), placeholderData: keepPreviousData })
  return (
    <div>
      <PageHeader title="Open jobs" description="Search real listings from clients on SkillBridge NG." />
      <div className="mb-6 grid gap-3 sm:grid-cols-4">
        <div className="sm:col-span-2"><SearchInput value={f.search} onChange={(v) => set({ search: v })} placeholder="Search jobs" /></div>
        <Select aria-label="Job type" value={f.jobType} onChange={(e) => set({ jobType: e.target.value })}>
          <option value="">All job types</option><option value="FULL_TIME">Full time</option><option value="PART_TIME">Part time</option><option value="CONTRACT">Contract</option><option value="FREELANCE">Freelance</option><option value="INTERNSHIP">Internship</option>
        </Select>
        <Select aria-label="Experience level" value={f.experienceLevel} onChange={(e) => set({ experienceLevel: e.target.value })}>
          <option value="">Any experience</option><option value="ENTRY">Entry</option><option value="MID">Mid</option><option value="SENIOR">Senior</option>
        </Select>
      </div>
      {q.isLoading ? <SkeletonList /> : q.isError ? <ErrorState error={q.error} onRetry={q.refetch} /> :
        q.data?.content?.length === 0 ? <EmptyState icon={Briefcase} title="No jobs match these filters" description="Clear a filter or search for something broader." /> : (
        <>
          <div className="space-y-3">
            {q.data.content.map((j) => (
              <Link key={j.id} to={`/jobs/${j.id}`} className="block">
                <Card className="p-4 transition-colors hover:border-primary/60">
                  <div className="flex flex-wrap items-start justify-between gap-2">
                    <h3 className="font-semibold">{j.title}</h3>
                    {j.status && <StatusBadge status={j.status} />}
                  </div>
                  <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-muted">
                    {j.location && <span className="inline-flex items-center gap-1"><MapPin className="h-3.5 w-3.5" />{j.location}</span>}
                    {jobBudget(j) && <span>{jobBudget(j)}</span>}
                    {j.createdAt && <span>Posted {fmtDate(j.createdAt)}</span>}
                  </div>
                  <div className="mt-2 flex gap-2">{j.jobType && <Badge variant="info">{label(j.jobType)}</Badge>}{j.experienceLevel && <Badge>{label(j.experienceLevel)}</Badge>}</div>
                </Card>
              </Link>
            ))}
          </div>
          <Pagination page={q.data} onPage={(p) => set({ page: p })} />
        </>
      )}
    </div>
  )
}
