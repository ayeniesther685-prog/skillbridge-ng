import { z } from 'zod'
import { useQuery } from '@tanstack/react-query'
import AutoForm from '../../components/ui/AutoForm'
import { jobsApi } from '../../api/jobs'
import { optNum, optStr, reqStr } from '../../utils/validators'

const schema = z.object({
  title: reqStr('Enter a job title', 200), description: reqStr('Describe the job'), location: optStr(),
  jobType: optStr(), experienceLevel: optStr(), budgetMin: optNum(), budgetMax: optNum(), deadline: optStr(), skillIds: z.array(z.number()).optional(),
}).refine((v) => v.budgetMin == null || v.budgetMax == null || v.budgetMin <= v.budgetMax, { path: ['budgetMax'], message: 'Maximum must be at least the minimum' })

export default function JobForm({ job, onSubmit, submitLabel, onCancel }) {
  const skills = useQuery({ queryKey: ['skills', ''], queryFn: () => jobsApi.skills('') })
  const list = Array.isArray(skills.data) ? skills.data : skills.data?.content || []
  const fields = [
    { name: 'title', label: 'Job title', wide: true },
    { name: 'description', label: 'Description', type: 'textarea' },
    { name: 'location', label: 'Location', placeholder: 'Lagos or Remote' },
    { name: 'jobType', label: 'Job type', type: 'select', options: [['FULL_TIME', 'Full time'], ['PART_TIME', 'Part time'], ['CONTRACT', 'Contract'], ['FREELANCE', 'Freelance'], ['INTERNSHIP', 'Internship']] },
    { name: 'experienceLevel', label: 'Experience level', type: 'select', options: [['ENTRY', 'Entry'], ['MID', 'Mid'], ['SENIOR', 'Senior']] },
    { name: 'deadline', label: 'Deadline', type: 'date' },
    { name: 'budgetMin', label: 'Minimum budget (₦)', type: 'number' },
    { name: 'budgetMax', label: 'Maximum budget (₦)', type: 'number' },
    { name: 'skillIds', label: 'Skills needed', type: 'multiselect', options: list.map((s) => [s.id, s.name]) },
  ]
  const dv = Object.fromEntries(fields.map((f) => [f.name, f.name === 'skillIds' ? (job?.skills || []).map((s) => s.id ?? s) : job?.[f.name] ?? '']))
  return <AutoForm key={job?.id ?? 'new'} fields={fields} schema={schema} defaultValues={dv} onSubmit={onSubmit} submitLabel={submitLabel} onCancel={onCancel} />
}
