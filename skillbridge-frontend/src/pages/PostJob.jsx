import { useNavigate } from 'react-router-dom'
import * as jobApi from '../api/jobApi.js'
import { Card, PageHeader } from '../components/ui.jsx'
import JobForm from '../components/JobForm.jsx'
import { useToast } from '../components/Toast.jsx'

export default function PostJob() {
  const navigate = useNavigate()
  const toast = useToast()
  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title="Post a job" subtitle="Describe the work and the skills you need." />
      <Card>
        <JobForm submitLabel="Publish job" onSubmit={async (body) => { const job = await jobApi.create(body); toast.success('Job posted.'); navigate(`/jobs/${job.id}`) }} />
      </Card>
    </div>
  )
}
