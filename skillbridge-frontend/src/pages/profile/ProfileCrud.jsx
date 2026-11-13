import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { profileApi } from '../../api/profile'
import { useAuth } from '../../context/AuthContext'
import { useSave } from '../../hooks/useApi'
import AutoForm from '../../components/ui/AutoForm'
import Dialog from '../../components/ui/Dialog'
import Button from '../../components/ui/Button'
import Card, { CardBody } from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import QueryBoundary from '../../components/ui/QueryBoundary'
import { optStr, optUrl, reqStr } from '../../utils/validators'
import { fmtDate } from '../../utils/format'

const CONFIG = {
  education: { title: 'Education', main: 'institution', sub: ['degree', 'fieldOfStudy'], schema: { institution: reqStr('Enter the institution'), degree: optStr(), fieldOfStudy: optStr(), startDate: optStr(), endDate: optStr(), description: optStr() },
    fields: [{ name: 'institution', label: 'Institution' }, { name: 'degree', label: 'Degree' }, { name: 'fieldOfStudy', label: 'Field of study' }, { name: 'startDate', label: 'Start date', type: 'date' }, { name: 'endDate', label: 'End date', type: 'date' }, { name: 'description', label: 'Description', type: 'textarea' }] },
  experience: { title: 'Experience', main: 'title', sub: ['company', 'location'], schema: { title: reqStr('Enter the job title'), company: reqStr('Enter the company'), location: optStr(), startDate: optStr(), endDate: optStr(), description: optStr() },
    fields: [{ name: 'title', label: 'Job title' }, { name: 'company', label: 'Company' }, { name: 'location', label: 'Location' }, { name: 'startDate', label: 'Start date', type: 'date' }, { name: 'endDate', label: 'End date', type: 'date' }, { name: 'description', label: 'Description', type: 'textarea' }] },
  certifications: { title: 'Certifications', main: 'name', sub: ['issuingOrganization'], schema: { name: reqStr('Enter the certification name'), issuingOrganization: optStr(), issueDate: optStr(), expiryDate: optStr(), credentialUrl: optUrl() },
    fields: [{ name: 'name', label: 'Name' }, { name: 'issuingOrganization', label: 'Issuing organisation' }, { name: 'issueDate', label: 'Issue date', type: 'date' }, { name: 'expiryDate', label: 'Expiry date', type: 'date' }, { name: 'credentialUrl', label: 'Credential link' }] },
  portfolio: { title: 'Portfolio', main: 'title', sub: ['projectUrl'], schema: { title: reqStr('Enter a title'), description: optStr(), projectUrl: optUrl(), imageUrl: optUrl() },
    fields: [{ name: 'title', label: 'Title' }, { name: 'projectUrl', label: 'Project link' }, { name: 'imageUrl', label: 'Image link', hint: 'Paste a link — file upload isn’t available yet.' }, { name: 'description', label: 'Description', type: 'textarea' }] },
}
const arr = (d) => (Array.isArray(d) ? d : d?.content || [])

export default function ProfileCrud({ kind }) {
  const c = CONFIG[kind]
  const { user } = useAuth()
  const [editing, setEditing] = useState(null) // null | {} (new) | item
  const q = useQuery({ queryKey: ['profile', kind, user.id], queryFn: () => profileApi.list(user.id, kind) })
  const inv = { invalidate: [['profile', kind]] }
  const create = useSave((b) => profileApi.create(kind, b), { ...inv, success: 'Added', onSuccess: () => setEditing(null) })
  const update = useSave((b) => profileApi.update_item(kind, b), { ...inv, success: 'Saved', onSuccess: () => setEditing(null) })
  const remove = useSave((id) => profileApi.remove(kind, id), { ...inv, success: 'Removed' })
  const schema = z.object(c.schema)
  return (
    <div>
      <div className="mb-4 flex justify-end"><Button size="sm" onClick={() => setEditing({})}><Plus className="h-4 w-4" />Add</Button></div>
      <QueryBoundary q={q}>{(d) => arr(d).length === 0
        ? <EmptyState title={`No ${c.title.toLowerCase()} yet`} description="Add your first entry so it shows on your profile." action={<Button size="sm" onClick={() => setEditing({})}>Add entry</Button>} />
        : <div className="space-y-3">{arr(d).map((x) => (
            <Card key={x.id}><CardBody className="flex items-start justify-between gap-3">
              <div className="min-w-0 text-sm">
                <p className="font-medium">{x[c.main]}</p>
                <p className="text-muted">{c.sub.map((k) => x[k]).filter(Boolean).join(' · ')}</p>
                {(x.startDate || x.issueDate) && <p className="text-xs text-muted">{fmtDate(x.startDate || x.issueDate)}{x.endDate ? ` – ${fmtDate(x.endDate)}` : ''}</p>}
              </div>
              <div className="flex shrink-0 gap-1">
                <Button variant="ghost" size="sm" aria-label="Edit" onClick={() => setEditing(x)}><Pencil className="h-4 w-4" /></Button>
                <Button variant="ghost" size="sm" aria-label="Delete" loading={remove.isPending && remove.variables === x.id} onClick={() => window.confirm('Remove this entry?') && remove.mutate(x.id)}><Trash2 className="h-4 w-4 text-danger" /></Button>
              </div>
            </CardBody></Card>))}</div>}
      </QueryBoundary>
      <Dialog open={!!editing} onClose={() => setEditing(null)} title={editing?.id ? `Edit ${c.title.toLowerCase()}` : `Add ${c.title.toLowerCase()}`}>
        {editing && <AutoForm key={editing.id ?? 'new'} fields={c.fields} schema={schema} defaultValues={Object.fromEntries(c.fields.map((f) => [f.name, editing[f.name] ?? '']))}
          onSubmit={(v) => (editing.id ? update.mutateAsync({ id: editing.id, ...v }) : create.mutateAsync(v))} onCancel={() => setEditing(null)} />}
      </Dialog>
    </div>
  )
}
