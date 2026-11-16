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

const applicationSchema = z.object({
  coverLetter: reqStr('Write a short proposal', 5000),
  expectedBudget: optNum(),
  proposedPrice: optNum(),
  estimatedDeliveryDays: optNum(),
})

export default function ApplyJob() {
  const { id } = useParams()
  const nav = useNavigate()
  const job = useQuery({ queryKey: ['job', id], queryFn: () => jobsApi.get(id) })

  const apply = useSave(applicationsApi.apply, {
    invalidate: [['applications']],
    success: 'Application sent',
    onSuccess: () => nav('/applications'),
  })

  return (
    <div className="max-w-2xl">
      <Breadcrumbs
        items={[
          ['Jobs', '/jobs'],
          [job.data?.title || 'Job', `/jobs/${id}`],
          ['Apply'],
        ]}
      />
      <PageHeader
        title="Apply for this job"
        description="Explain why you're a good fit and what you'd charge."
      />
      <Card>
        <CardBody>
          <AutoForm
            schema={applicationSchema}
            defaultValues={{
              coverLetter: '',
              expectedBudget: '',
              proposedPrice: '',
              estimatedDeliveryDays: 14,
            }}
            fields={[
              {
                name: 'coverLetter',
                label: 'Proposal',
                type: 'textarea',
                hint: "Mention relevant experience and how you'd approach the work.",
              },
              {
                name: 'expectedBudget',
                label: 'Your expected budget (₦)',
                type: 'number',
              },
              {
                name: 'proposedPrice',
                label: 'Your proposed price (₦)',
                type: 'number',
              },
              {
                name: 'estimatedDeliveryDays',
                label: 'Estimated delivery (days)',
                type: 'number',
              },
            ]}
            onSubmit={(v) =>
              apply.mutateAsync({
                jobId: id,
                expectedBudget: v.expectedBudget ? Number(v.expectedBudget) : null,
                proposal: {
                  coverLetter: v.coverLetter,
                  proposedPrice: Number(v.proposedPrice),
                  estimatedDeliveryDays: v.estimatedDeliveryDays
                    ? Number(v.estimatedDeliveryDays)
                    : null,
                },
              })
            }
            submitLabel="Send application"
            onCancel={() => nav(-1)}
          />
        </CardBody>
      </Card>
    </div>
  )
}