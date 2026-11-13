import { useNavigate, useParams } from 'react-router-dom'
import { z } from 'zod'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import { adminApi } from '../../api/admin'
import { useSave } from '../../hooks/useApi'
import { optNum, optStr, reqStr } from '../../utils/validators'
export default function CreateAssignment() {
  const { id } = useParams(); const nav = useNavigate()
  const create = useSave(adminApi.createAssignment, { invalidate: [['assignments', id]], success: 'Assignment created', onSuccess: () => nav('/admin/courses') })
  return (
    <div className="max-w-2xl">
      <Breadcrumbs items={[['Courses', '/admin/courses'], [`Course #${id}`], ['New assignment']]} />
      <PageHeader title="Create assignment" />
      <Card><CardBody><AutoForm schema={z.object({ title: reqStr('Enter a title', 200), description: reqStr('Describe the task'), dueDate: optStr(), maxScore: optNum() })} defaultValues={{ title: '', description: '', dueDate: '', maxScore: '' }}
        fields={[{ name: 'title', label: 'Title', wide: true }, { name: 'description', label: 'Instructions', type: 'textarea' }, { name: 'dueDate', label: 'Due date', type: 'date' }, { name: 'maxScore', label: 'Max score', type: 'number' }]}
        onSubmit={(v) => create.mutateAsync({ courseId: id, ...v })} submitLabel="Create assignment" onCancel={() => nav(-1)} /></CardBody></Card>
    </div>
  )
}
