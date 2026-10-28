import { GraduationCap } from 'lucide-react'
import ResourceManager from '../components/ResourceManager.jsx'
import { educationApi } from '../api/educationApi.js'
import { formatDate } from '../utils/dates.js'

const fields = [
  { name: 'institution', label: 'Institution', required: true, max: 200 },
  { name: 'degree', label: 'Degree', max: 150 },
  { name: 'fieldOfStudy', label: 'Field of study', max: 150 },
  { name: 'startDate', label: 'Start date', type: 'date' },
  { name: 'endDate', label: 'End date', type: 'date' },
  { name: 'description', label: 'Description', type: 'textarea', max: 1000 },
]

export default function Education() {
  return (
    <ResourceManager title="Education" subtitle="Where you studied." noun="Education" icon={GraduationCap} api={educationApi} fields={fields}
      renderItem={(x) => (<><p className="font-extrabold">{x.institution}</p><p className="text-sm text-slate-300">{[x.degree, x.fieldOfStudy].filter(Boolean).join(' · ')}</p>
        <p className="text-xs text-muted">{formatDate(x.startDate)} – {formatDate(x.endDate)}</p>{x.description && <p className="mt-2 text-sm text-slate-300">{x.description}</p>}</>)} />
  )
}
