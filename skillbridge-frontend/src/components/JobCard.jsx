import { Link } from 'react-router-dom'
import { Calendar, MapPin } from 'lucide-react'
import { Badge } from './ui.jsx'
import SkillPill from './SkillPill.jsx'
import { budgetRange, formatDate, label } from '../utils/dates.js'

export default function JobCard({ job }) {
  return (
    <Link to={`/jobs/${job.id}`} className="block rounded-2xl border border-line bg-ink-900 p-5 transition-colors hover:border-brand-400/60">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="truncate text-base font-extrabold">{job.title}</h3>
          <p className="mt-0.5 text-sm text-muted">{job.clientName}</p>
        </div>
        <Badge value={job.status} />
      </div>
      <p className="mt-3 line-clamp-2 text-sm text-slate-300">{job.description}</p>
      <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted">
        <span className="font-bold text-slate-200">{budgetRange(job.budgetMin, job.budgetMax)}</span>
        {job.location && <span className="inline-flex items-center gap-1"><MapPin className="h-3.5 w-3.5" />{job.location}</span>}
        <span>{label(job.jobType)} · {label(job.experienceLevel)}</span>
        {job.deadline && <span className="inline-flex items-center gap-1"><Calendar className="h-3.5 w-3.5" />{formatDate(job.deadline)}</span>}
      </div>
      {job.requiredSkills?.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1.5">{job.requiredSkills.slice(0, 5).map((s) => <SkillPill key={s.id} skill={s} />)}</div>
      )}
    </Link>
  )
}
