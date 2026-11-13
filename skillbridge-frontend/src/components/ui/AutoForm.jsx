import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import Input from './Input'
import Textarea from './Textarea'
import Select from './Select'
import Button from './Button'
import { normalizeError } from '../../api/errors'
import { useToast } from '../../context/ToastContext'

/**
 * Declarative form: fields = [{ name, label, type, options, hint, placeholder }]
 * type: text | textarea | number | date | url | email | select | multiselect
 * Validates with Zod, surfaces backend fieldErrors inline, strips empty strings before submit.
 */
export default function AutoForm({ fields, schema, defaultValues = {}, onSubmit, submitLabel = 'Save', onCancel }) {
  const toast = useToast()
  const { register, control, handleSubmit, setError, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(schema), defaultValues })
  const submit = async (values) => {
    const body = Object.fromEntries(Object.entries(values).filter(([, v]) => v !== '' && v !== undefined && !(typeof v === 'number' && Number.isNaN(v))))
    try { await onSubmit(body) } catch (err) {
      const e = normalizeError(err)
      if (e.fieldErrors) Object.entries(e.fieldErrors).forEach(([k, m]) => setError(k, { message: String(m) }))
      else toast.error(e.message)
    }
  }
  return (
    <form onSubmit={handleSubmit(submit)} noValidate className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2">
        {fields.map((f) => {
          const err = errors[f.name]?.message
          const wide = f.type === 'textarea' || f.type === 'multiselect' || f.wide ? 'sm:col-span-2' : ''
          if (f.type === 'textarea') return <Textarea key={f.name} className={wide} label={f.label} hint={f.hint} placeholder={f.placeholder} error={err} {...register(f.name)} />
          if (f.type === 'select') return (
            <div key={f.name} className={wide}>
              <Select label={f.label} {...register(f.name)}><option value="">{f.placeholder || 'Select…'}</option>{f.options.map((o) => { const [v, l] = Array.isArray(o) ? o : [o, o]; return <option key={v} value={v}>{l}</option> })}</Select>
              {err && <p className="mt-1 text-xs text-danger">{err}</p>}
            </div>)
          if (f.type === 'multiselect') return (
            <Controller key={f.name} name={f.name} control={control} render={({ field }) => (
              <fieldset className={wide}>
                <legend className="mb-1.5 text-sm font-medium">{f.label}</legend>
                <div className="flex flex-wrap gap-2">
                  {f.options.length === 0 && <span className="text-sm text-muted">Nothing to choose from.</span>}
                  {f.options.map(([v, l]) => { const on = (field.value || []).includes(v); return (
                    <button type="button" key={v} aria-pressed={on} onClick={() => field.onChange(on ? field.value.filter((x) => x !== v) : [...(field.value || []), v])}
                      className={`min-h-[36px] rounded border px-3 py-1 text-sm ${on ? 'border-primary bg-primary/15' : 'text-muted hover:text-fg'}`}>{l}</button>) })}
                </div>
                {err && <p className="mt-1 text-xs text-danger">{err}</p>}
              </fieldset>)} />)
          return <Input key={f.name} className={wide} label={f.label} type={f.type || 'text'} step={f.type === 'number' ? 'any' : undefined} hint={f.hint} placeholder={f.placeholder} error={err} {...register(f.name)} />
        })}
      </div>
      <div className="sticky bottom-0 -mx-5 flex justify-end gap-2 border-t bg-surface px-5 py-3 sm:static sm:mx-0 sm:border-0 sm:bg-transparent sm:px-0 sm:py-0">
        {onCancel && <Button type="button" variant="outline" onClick={onCancel}>Cancel</Button>}
        <Button type="submit" loading={isSubmitting}>{submitLabel}</Button>
      </div>
    </form>
  )
}
