import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { z } from 'zod'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import { adminApi } from '../../api/admin'
import { useSave } from '../../hooks/useApi'
import { num, optStr } from '../../utils/validators'
export default function GradeSubmission() {
  const { id } = useParams(); const { state } = useLocation(); const nav = useNavigate()
  const s = state?.submission
  const grade = useSave(adminApi.grade, { invalidate: [['admin', 'submissions']], success: 'Grade saved', onSuccess: () => nav(-1) })
  return (
    <div className="max-w-2xl">
      <Breadcrumbs items={[['Courses', '/admin/courses'], [`Submission #${id}`]]} />
      <PageHeader title="Grade submission" />
      {s && <Card className="mb-4"><CardBody className="space-y-2 text-sm">{s.content && <p className="whitespace-pre-line">{s.content}</p>}{s.fileUrl && <a className="text-accent hover:underline" href={s.fileUrl} target="_blank" rel="noreferrer noopener">Open submitted link</a>}</CardBody></Card>}
      <Card><CardBody><AutoForm schema={z.object({ score: num('Enter a score').pipe(z.number().min(0, 'Cannot be negative')), feedback: optStr() })} defaultValues={{ score: s?.score ?? '', feedback: s?.feedback ?? '' }}
        fields={[{ name: 'score', label: 'Score', type: 'number' }, { name: 'feedback', label: 'Feedback', type: 'textarea' }]} onSubmit={(v) => grade.mutateAsync({ id, ...v })} submitLabel="Save grade" onCancel={() => nav(-1)} /></CardBody></Card>
    </div>
  )
}
