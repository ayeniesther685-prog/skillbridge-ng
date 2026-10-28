import { useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import { useFetch } from '../utils/hooks.js'
import { getErrorMessage } from '../utils/errors.js'
import { useToast } from './Toast.jsx'
import { Button, Card, Field, Modal, PageHeader, inputCls, textareaCls } from './ui.jsx'
import LoadingSpinner from './LoadingSpinner.jsx'
import ErrorMessage from './ErrorMessage.jsx'
import EmptyState from './EmptyState.jsx'

const emptyForm = (fields) => Object.fromEntries(fields.map((f) => [f.name, f.type === 'checkbox' ? false : '']))
const toForm = (fields, item) => Object.fromEntries(fields.map((f) => [f.name, f.type === 'checkbox' ? !!item[f.name] : item[f.name] ?? '']))

/**
 * Generic CRUD page for the user's own education / experience / certifications / portfolio.
 * api: { list(userId), create(body), update(id, body), remove(id) }
 */
export default function ResourceManager({ title, subtitle, noun, icon, api, fields, renderItem }) {
  const { user } = useAuth()
  const toast = useToast()
  const { data: items, loading, error, reload } = useFetch(() => api.list(user.id), [user.id])
  const [editing, setEditing] = useState(null) // null | 'new' | item
  const [form, setForm] = useState({})
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const openNew = () => { setForm(emptyForm(fields)); setFormError(''); setEditing('new') }
  const openEdit = (item) => { setForm(toForm(fields, item)); setFormError(''); setEditing(item) }
  const set = (name, value) => setForm((f) => ({ ...f, [name]: value }))

  const submit = async (e) => {
    e.preventDefault()
    if (saving) return
    const body = {}
    for (const f of fields) {
      const v = form[f.name]
      if (f.type === 'checkbox') { body[f.name] = !!v; continue }
      const t = typeof v === 'string' ? v.trim() : v
      if (f.required && !t) return setFormError(`${f.label} is required.`)
      if (f.max && t && t.length > f.max) return setFormError(`${f.label} must be ${f.max} characters or fewer.`)
      body[f.name] = t || null
    }
    if (body.current === true && 'endDate' in body) body.endDate = null
    if (body.startDate && body.endDate && body.endDate < body.startDate) return setFormError('End date cannot be before the start date.')
    setSaving(true); setFormError('')
    try {
      if (editing === 'new') await api.create(body)
      else await api.update(editing.id, body)
      toast.success(`${noun} ${editing === 'new' ? 'added' : 'updated'}.`)
      setEditing(null)
      reload(true)
    } catch (err) {
      setFormError(getErrorMessage(err))
    } finally { setSaving(false) }
  }

  const remove = async (item) => {
    if (!window.confirm(`Delete this ${noun.toLowerCase()}?`)) return
    try { await api.remove(item.id); toast.success(`${noun} deleted.`); reload(true) } catch (err) { toast.error(getErrorMessage(err)) }
  }

  return (
    <div>
      <PageHeader title={title} subtitle={subtitle} actions={<Button onClick={openNew}><Plus className="h-4 w-4" />Add {noun.toLowerCase()}</Button>} />
      {loading ? <LoadingSpinner /> : error ? <ErrorMessage error={error} onRetry={reload} /> : !items?.length ? (
        <EmptyState icon={icon} title={`No ${title.toLowerCase()} yet`} text={`Add your first ${noun.toLowerCase()} to strengthen your profile.`} action={<Button onClick={openNew}>Add {noun.toLowerCase()}</Button>} />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {items.map((item) => (
            <Card key={item.id} className="flex flex-col justify-between gap-4">
              <div>{renderItem(item)}</div>
              <div className="flex gap-2">
                <Button size="sm" variant="secondary" onClick={() => openEdit(item)}><Pencil className="h-3.5 w-3.5" />Edit</Button>
                <Button size="sm" variant="danger" onClick={() => remove(item)}><Trash2 className="h-3.5 w-3.5" />Delete</Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal open={!!editing} onClose={() => !saving && setEditing(null)} title={`${editing === 'new' ? 'Add' : 'Edit'} ${noun.toLowerCase()}`}>
        <form onSubmit={submit} className="space-y-4" noValidate>
          {formError && <ErrorMessage message={formError} />}
          {fields.map((f) =>
            f.type === 'checkbox' ? (
              <label key={f.name} className="flex items-center gap-2 text-sm text-slate-200">
                <input type="checkbox" checked={form[f.name]} onChange={(e) => set(f.name, e.target.checked)} className="h-4 w-4 accent-brand-500" />{f.label}
              </label>
            ) : (
              <Field key={f.name} label={`${f.label}${f.required ? ' *' : ''}`} hint={f.hint}>
                {f.type === 'textarea' ? (
                  <textarea className={textareaCls} value={form[f.name]} maxLength={f.max} placeholder={f.placeholder} onChange={(e) => set(f.name, e.target.value)} />
                ) : (
                  <input className={inputCls} type={f.type === 'date' ? 'date' : f.type === 'url' ? 'url' : 'text'} value={form[f.name]} maxLength={f.max}
                    placeholder={f.placeholder} disabled={f.name === 'endDate' && form.current} onChange={(e) => set(f.name, e.target.value)} />
                )}
              </Field>
            )
          )}
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="secondary" onClick={() => setEditing(null)} disabled={saving}>Cancel</Button>
            <Button type="submit" loading={saving}>{saving ? 'Saving…' : 'Save'}</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
