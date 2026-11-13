import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import PageHeader from '../../components/ui/PageHeader'
import Breadcrumbs from '../../components/ui/Breadcrumbs'
import Card, { CardBody } from '../../components/ui/Card'
import AutoForm from '../../components/ui/AutoForm'
import CoursePicker from '../academy/CoursePicker'
import { adminApi } from '../../api/admin'
import { useSave } from '../../hooks/useApi'
import { optNum, optStr, reqStr } from '../../utils/validators'
import { useToast } from '../../context/ToastContext'
export default function CreateCourse() {
  const nav = useNavigate(); const toast = useToast()
  const [sel, setSel] = useState({})
  const create = useSave(adminApi.createCourse, { invalidate: [['courses']], success: 'Course created', onSuccess: () => nav('/admin/courses') })
  return (
    <div className="max-w-2xl">
      <Breadcrumbs items={[['Courses', '/admin/courses'], ['New course']]} />
      <PageHeader title="Create course" />
      <Card className="mb-4"><CardBody><CoursePicker withCourse={false} onChange={setSel} /></CardBody></Card>
      <Card><CardBody><AutoForm schema={z.object({ code: optStr(), title: reqStr('Enter a title', 200), description: optStr(), creditUnits: optNum() })} defaultValues={{ code: '', title: '', description: '', creditUnits: '' }}
        fields={[{ name: 'title', label: 'Title', wide: true }, { name: 'code', label: 'Course code' }, { name: 'creditUnits', label: 'Credit units', type: 'number' }, { name: 'description', label: 'Description', type: 'textarea' }]}
        onSubmit={(v) => { if (!sel.departmentId) { toast.error('Choose a department first'); return Promise.resolve() } return create.mutateAsync({ departmentId: Number(sel.departmentId), ...v }) }} submitLabel="Create course" onCancel={() => nav(-1)} /></CardBody></Card>
    </div>
  )
}
