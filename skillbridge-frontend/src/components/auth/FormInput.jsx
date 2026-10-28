import { forwardRef, useId } from 'react'

const FormInput = forwardRef(function FormInput({ label, icon: Icon, error, rightSlot, id, className = '', ...props }, ref) {
  const auto = useId()
  const inputId = id ?? auto
  const errId = `${inputId}-error`
  return (
    <div className={className}>
      <label htmlFor={inputId} className="mb-1.5 block text-sm font-semibold text-slate-200">{label}</label>
      <div className="relative">
        {Icon && <Icon className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden="true" />}
        <input id={inputId} ref={ref} aria-invalid={!!error} aria-describedby={error ? errId : undefined}
          className={`h-12 w-full rounded-xl border bg-ink-800 text-sm text-white placeholder:text-slate-500 transition-colors focus:border-brand-400 focus:outline-none focus:ring-2 focus:ring-brand-400/30 ${Icon ? 'pl-10' : 'pl-4'} ${rightSlot ? 'pr-12' : 'pr-4'} ${error ? 'border-red-500/70' : 'border-line'}`}
          {...props} />
        {rightSlot && <div className="absolute right-2 top-1/2 -translate-y-1/2">{rightSlot}</div>}
      </div>
      {error && <p id={errId} role="alert" className="mt-1.5 text-xs font-medium text-red-300">Error: {error}</p>}
    </div>
  )
})
export default FormInput
