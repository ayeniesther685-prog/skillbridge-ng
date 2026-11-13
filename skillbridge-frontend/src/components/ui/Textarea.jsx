import { forwardRef } from 'react'
const Textarea = forwardRef(function Textarea({ label, error, hint, className = '', id, rows = 4, ...rest }, ref) {
  const key = id || rest.name
  return (
    <div className={className}>
      {label && <label htmlFor={key} className="mb-1.5 block text-sm font-medium">{label}</label>}
      <textarea id={key} ref={ref} rows={rows} aria-invalid={!!error} className={`field ${error ? 'border-danger' : ''}`} {...rest} />
      {error ? <p className="mt-1 text-xs text-danger">{error}</p> : hint ? <p className="mt-1 text-xs text-muted">{hint}</p> : null}
    </div>
  )
})
export default Textarea
