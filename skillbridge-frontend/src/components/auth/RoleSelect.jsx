const OPTIONS = [
  { value: 'STUDENT', label: 'Student' },
  { value: 'FREELANCER', label: 'Freelancer' },
  { value: 'CLIENT', label: 'Client' },
]

export default function RoleSelect({ value = [], onChange, error }) {
  const toggle = (v) => onChange(value.includes(v) ? value.filter((r) => r !== v) : [...value, v])
  return (
    <fieldset aria-describedby={error ? 'roles-error' : undefined}>
      <legend className="mb-1.5 text-sm font-semibold text-slate-200">I want to join as</legend>
      <div className="grid gap-2 sm:grid-cols-3">
        {OPTIONS.map((o) => {
          const on = value.includes(o.value)
          return (
            <label key={o.value} className={`flex cursor-pointer items-center justify-center rounded-xl border px-3 py-3 text-sm font-semibold transition-colors focus-within:outline-2 focus-within:outline-brand-400 ${on ? 'border-brand-400 bg-brand-500/15 text-white' : 'border-line bg-ink-800 text-slate-300 hover:border-slate-500'}`}>
              <input type="checkbox" className="sr-only" checked={on} onChange={() => toggle(o.value)} />
              {o.label}
            </label>
          )
        })}
      </div>
      <p className="mt-1.5 text-xs text-muted">Choose one or more.</p>
      {error && <p id="roles-error" role="alert" className="mt-1 text-xs font-medium text-red-300">Error: {error}</p>}
    </fieldset>
  )
}
