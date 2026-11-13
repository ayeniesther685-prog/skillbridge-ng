import { useNavigate, useParams } from 'react-router-dom'
import { z } from 'zod'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import { adminApi } from '../../api/admin'
import { useSave } from '../../hooks/useApi'
import { optNum, optUrl, reqStr } from '../../utils/validators'
export default function CreateLesson() {
  const { id } = useParams(); const nav = useNavigate()
  const create = useSave(adminApi.createLesson, { invalidate: [['lessons', id]], success: 'Lesson created', onSuccess: () => nav('/admin/courses') })
  return (
    <div className="max-w-2xl">
      <Breadcrumbs items={[['Courses', '/admin/courses'], [`Course #${id}`], ['New lesson']]} />
      <PageHeader title="Create lesson" />
      <Card><CardBody><AutoForm schema={z.object({ title: reqStr('Enter a title', 200), content: reqStr('Add lesson content', 20000), videoUrl: optUrl(), orderIndex: optNum() })} defaultValues={{ title: '', content: '', videoUrl: '', orderIndex: '' }}
        fields={[{ name: 'title', label: 'Title', wide: true }, { name: 'content', label: 'Content', type: 'textarea' }, { name: 'videoUrl', label: 'Video link' }, { name: 'orderIndex', label: 'Order', type: 'number' }]}
        onSubmit={(v) => create.mutateAsync({ courseId: id, ...v })} submitLabel="Create lesson" onCancel={() => nav(-1)} /></CardBody></Card>
    </div>
  )
}
