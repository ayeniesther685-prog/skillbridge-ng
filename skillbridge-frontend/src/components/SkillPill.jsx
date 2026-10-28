export default function SkillPill({ skill, active, onClick, children }) {
  const cls = `inline-flex items-center gap-1 rounded-full border px-3 py-1 text-xs font-semibold transition-colors ${
    active ? 'border-brand-400 bg-brand-500/20 text-white' : 'border-line bg-ink-800 text-slate-300'
  } ${onClick ? 'cursor-pointer hover:border-slate-500' : ''}`
  return onClick ? (
    <button type="button" onClick={onClick} aria-pressed={!!active} className={cls}>{skill?.name}{children}</button>
  ) : (
    <span className={cls}>{skill?.name}{children}</span>
  )
}
