import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import { jobsApi } from '../../api/jobs'
import { applicationsApi } from '../../api/applications'
import { useSave } from '../../hooks/useApi'
import { optNum, reqStr } from '../../utils/validators'
export default function ApplyJob() {
  const { id } = useParams()
  const nav = useNavigate()
  const job = useQuery({ queryKey: ['job', id], queryFn: () => jobsApi.get(id) })
  const apply = useSave(applicationsApi.apply, { invalidate: [['applications']], success: 'Application sent', onSuccess: () => nav('/applications') })
  return (
    <div className="max-w-2xl">
      <Breadcrumbs items={[['Jobs', '/jobs'], [job.data?.title || 'Job', `/jobs/${id}`], ['Apply']]} />
      <PageHeader title="Apply for this job" description="Explain why you're a good fit and what you'd charge." />
      <Card><CardBody>
        <AutoForm schema={z.object({ coverLetter: reqStr('Write a short proposal', 5000), proposedAmount: optNum() })} defaultValues={{ coverLetter: '', proposedAmount: '' }}
          fields={[{ name: 'coverLetter', label: 'Proposal', type: 'textarea', hint: 'Mention relevant experience and how you’d approach the work.' }, { name: 'proposedAmount', label: 'Your price (₦)', type: 'number' }]}
          onSubmit={(v) => apply.mutateAsync({ jobId: id, ...v })} submitLabel="Send application" onCancel={() => nav(-1)} />
      </CardBody></Card>
    </div>
  )
}
