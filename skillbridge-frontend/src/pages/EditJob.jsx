import { useNavigate, useParams } from 'react-router-dom'
import * as jobApi from '../api/jobApi.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { Card, PageHeader } from '../components/ui.jsx'
import JobForm from '../components/JobForm.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useToast } from '../components/Toast.jsx'

export default function EditJob() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const { data: job, loading, error, reload } = useFetch(() => jobApi.get(id), [id])
  if (loading) return <LoadingSpinner />
  if (error) return <ErrorMessage error={error} onRetry={reload} />
  if (job.clientId !== user.id) return <ErrorMessage message="Only the person who posted this job can edit it." />
  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title="Edit job" />
      <Card>
        <JobForm initial={job} submitLabel="Save changes" onSubmit={async (body) => { await jobApi.update(job.id, body); toast.success('Job updated.'); navigate(`/jobs/${job.id}`) }} />
      </Card>
    </div>
  )
}
