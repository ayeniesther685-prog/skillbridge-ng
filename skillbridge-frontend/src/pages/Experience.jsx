import { Briefcase } from 'lucide-react'
import ResourceManager from '../components/ResourceManager.jsx'
import { experienceApi } from '../api/experienceApi.js'
import { formatDate } from '../utils/dates.js'

const fields = [
  { name: 'company', label: 'Company', required: true, max: 200 },
  { name: 'position', label: 'Position', required: true, max: 150 },
  { name: 'startDate', label: 'Start date', type: 'date' },
  { name: 'current', label: 'I currently work here', type: 'checkbox' },
  { name: 'endDate', label: 'End date', type: 'date' },
  { name: 'description', label: 'Description', type: 'textarea', max: 1000 },
]

export default function Experience() {
  return (
    <ResourceManager title="Work experience" subtitle="Roles you've held." noun="Experience" icon={Briefcase} api={experienceApi} fields={fields}
      renderItem={(x) => (<><p className="font-extrabold">{x.position}</p><p className="text-sm text-slate-300">{x.company}</p>
        <p className="text-xs text-muted">{formatDate(x.startDate)} – {x.current ? 'Present' : formatDate(x.endDate)}</p>{x.description && <p className="mt-2 text-sm text-slate-300">{x.description}</p>}</>)} />
  )
}
