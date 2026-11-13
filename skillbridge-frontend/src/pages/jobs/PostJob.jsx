import { useNavigate } from 'react-router-dom'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import JobForm from './JobForm'
import { jobsApi } from '../../api/jobs'
import { useSave } from '../../hooks/useApi'
export default function PostJob() {
  const nav = useNavigate()
  const create = useSave(jobsApi.create, { invalidate: [['jobs']], success: 'Job posted', onSuccess: (j) => nav(j?.id ? `/jobs/${j.id}` : '/jobs') })
  return (
    <div className="max-w-3xl">
      <Breadcrumbs items={[['Jobs', '/jobs'], ['Post a job']]} />
      <PageHeader title="Post a job" description="Describe the work and what you can pay. Freelancers apply from the job page." />
      <Card><CardBody><JobForm onSubmit={(v) => create.mutateAsync(v)} submitLabel="Post job" onCancel={() => nav(-1)} /></CardBody></Card>
    </div>
  )
}
