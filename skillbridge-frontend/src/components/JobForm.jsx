import { useState } from 'react'
import { useFetch } from '../utils/hooks.js'
import * as skillApi from '../api/skillApi.js'
import { getErrorMessage } from '../utils/errors.js'
import { Button, Field, inputCls, textareaCls } from './ui.jsx'
import SkillPill from './SkillPill.jsx'
import ErrorMessage from './ErrorMessage.jsx'

export const JOB_TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'FREELANCE', 'INTERNSHIP', 'REMOTE']
export const LEVELS = ['ENTRY', 'INTERMEDIATE', 'EXPERT']

export default function JobForm({ initial, submitLabel, onSubmit }) {
  const { data: catalog } = useFetch(() => skillApi.list(), [])
  const [f, setF] = useState({
    title: initial?.title ?? '', description: initial?.description ?? '',
    budgetMin: initial?.budgetMin ?? '', budgetMax: initial?.budgetMax ?? '',
    location: initial?.location ?? '', jobType: initial?.jobType ?? 'FREELANCE', experienceLevel: initial?.experienceLevel ?? 'INTERMEDIATE',
    deadline: initial?.deadline ?? '', skillIds: initial?.requiredSkills?.map((s) => s.id) ?? [],
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const set = (k, v) => setF((s) => ({ ...s, [k]: v }))
  const toggle = (id) => set('skillIds', f.skillIds.includes(id) ? f.skillIds.filter((x) => x !== id) : [...f.skillIds, id])
  const num = (v) => (v === '' || v == null ? null : Number(v))

  const submit = async (e) => {
    e.preventDefault()
    if (saving) return
    const min = num(f.budgetMin), max = num(f.budgetMax)
    if (!f.title.trim()) return setError('Title is required.')
    if (!f.description.trim()) return setError('Description is required.')
    if ((min != null && min < 0) || (max != null && max < 0)) return setError('Budget cannot be negative.')
    if (min != null && max != null && max < min) return setError('Maximum budget must be at least the minimum budget.')
    setSaving(true); setError('')
    try {
      await onSubmit({
        title: f.title.trim(), description: f.description.trim(), budgetMin: min, budgetMax: max,
        location: f.location.trim() || null, jobType: f.jobType, experienceLevel: f.experienceLevel,
        deadline: f.deadline || null, skillIds: f.skillIds,
      })
    } catch (err) { setError(getErrorMessage(err)); setSaving(false) }
  }

  return (
    <form onSubmit={submit} noValidate className="space-y-5">
      {error && <ErrorMessage message={error} />}
      <Field label="Title *"><input className={inputCls} maxLength={200} value={f.title} onChange={(e) => set('title', e.target.value)} placeholder="e.g. Build a REST API" /></Field>
      <Field label="Description *" hint={`${f.description.length}/4000`}><textarea className={textareaCls} maxLength={4000} value={f.description} onChange={(e) => set('description', e.target.value)} /></Field>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Minimum budget (₦)"><input className={inputCls} type="number" min="0" value={f.budgetMin} onChange={(e) => set('budgetMin', e.target.value)} /></Field>
        <Field label="Maximum budget (₦)"><input className={inputCls} type="number" min="0" value={f.budgetMax} onChange={(e) => set('budgetMax', e.target.value)} /></Field>
        <Field label="Job type *"><select className={inputCls} value={f.jobType} onChange={(e) => set('jobType', e.target.value)}>{JOB_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}</select></Field>
        <Field label="Experience level *"><select className={inputCls} value={f.experienceLevel} onChange={(e) => set('experienceLevel', e.target.value)}>{LEVELS.map((t) => <option key={t} value={t}>{t}</option>)}</select></Field>
        <Field label="Location"><input className={inputCls} maxLength={150} value={f.location} onChange={(e) => set('location', e.target.value)} placeholder="Lagos" /></Field>
        <Field label="Deadline"><input className={inputCls} type="date" value={f.deadline} onChange={(e) => set('deadline', e.target.value)} /></Field>
      </div>
      <div>
        <p className="mb-2 text-sm font-semibold text-slate-200">Required skills</p>
        <div className="flex flex-wrap gap-2">{catalog?.map((s) => <SkillPill key={s.id} skill={s} active={f.skillIds.includes(s.id)} onClick={() => toggle(s.id)} />)}</div>
      </div>
      <Button type="submit" loading={saving}>{saving ? 'Saving…' : submitLabel}</Button>
    </form>
  )
}
