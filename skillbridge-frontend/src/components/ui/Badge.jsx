const v = {
  success: 'bg-success/15 text-success', warning: 'bg-warning/15 text-warning',
  danger: 'bg-danger/15 text-danger', info: 'bg-accent/15 text-accent', neutral: 'bg-fg/10 text-muted',
}
export default function Badge({ variant = 'neutral', children }) {
  return <span className={`inline-flex items-center rounded px-2 py-0.5 text-xs font-medium ${v[variant]}`}>{children}</span>
}
