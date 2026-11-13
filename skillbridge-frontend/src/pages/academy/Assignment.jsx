import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import Button from '../../components/ui/Button'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { academyApi } from '../../api/academy'
import { useSave } from '../../hooks/useApi'
import { fmtDate } from '../../utils/format'
import { optStr, optUrl } from '../../utils/validators'
const schema = z.object({ content: optStr(), fileUrl: optUrl() }).refine((v) => v.content?.trim() || v.fileUrl?.trim(), { path: ['content'], message: 'Write your answer or paste a link to your work' })
export default function Assignment() {
  const { id } = useParams()
  const q = useQuery({ queryKey: ['assignment', id], queryFn: () => academyApi.assignment(id) })
  const submit = useSave(academyApi.submit, { invalidate: [['submission', id]], success: 'Submitted' })
  return (
    <QueryBoundary q={q}>{(a) => (
      <div className="max-w-2xl">
        <Breadcrumbs items={[['My courses', '/academy/courses'], ['Assignment']]} />
        <PageHeader title={a.title} description={a.dueDate ? `Due ${fmtDate(a.dueDate)}` : undefined} actions={<Button as={Link} to={`/academy/assignments/${id}/my-submission`} variant="outline" size="sm">My submission</Button>} />
        {a.description && <Card className="mb-4"><CardBody className="whitespace-pre-line text-sm">{a.description}</CardBody></Card>}
        <Card><CardBody><AutoForm schema={schema} defaultValues={{ content: '', fileUrl: '' }}
          fields={[{ name: 'content', label: 'Your answer', type: 'textarea' }, { name: 'fileUrl', label: 'Link to your work', hint: 'Paste a link — file upload isn’t available yet.', wide: true }]}
          onSubmit={(v) => submit.mutateAsync({ id, ...v })} submitLabel="Submit assignment" /></CardBody></Card>
      </div>)}
    </QueryBoundary>
  )
}
