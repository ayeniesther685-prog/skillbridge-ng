export function getStrength(pw = '') {
  if (!pw) return { level: 0, label: '' }
  let score = 0
  if (pw.length >= 8) score++
  if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) score++
  if (/\d/.test(pw)) score++
  if (pw.length >= 12 || /[^A-Za-z0-9]/.test(pw)) score++
  if (score <= 2) return { level: 1, label: 'Weak' }
  if (score === 3) return { level: 2, label: 'Medium' }
  return { level: 3, label: 'Strong' }
}
const colors = ['', 'bg-red-500', 'bg-accent', 'bg-emerald-500']

export default function PasswordStrength({ password }) {
  const { level, label } = getStrength(password)
  if (!password) return null
  return (
    <div className="mt-2" aria-live="polite">
      <div className="flex gap-1.5" aria-hidden="true">
        {[1, 2, 3].map((i) => <div key={i} className={`h-1.5 flex-1 rounded-full transition-colors ${i <= level ? colors[level] : 'bg-ink-700'}`} />)}
      </div>
      <p className="mt-1.5 text-xs text-muted">Password strength: <span className="font-semibold text-slate-200">{label}</span></p>
    </div>
  )
}
