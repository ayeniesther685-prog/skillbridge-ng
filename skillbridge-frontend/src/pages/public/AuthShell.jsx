import { Link } from 'react-router-dom'
export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-bg px-4 py-10">
      <div className="w-full max-w-md">
        <Link to="/" className="mb-8 flex items-center justify-center gap-2 text-lg font-semibold tracking-tight"><span className="h-5 w-5 rounded bg-primary" />SkillBridge NG</Link>
        <div className="rounded-lg border bg-card p-6">
          <h1 className="text-xl font-semibold">{title}</h1>
          <p className="mt-1 text-sm text-muted">{subtitle}</p>
          <div className="mt-6">{children}</div>
        </div>
        <p className="mt-4 text-center text-sm text-muted">{footer}</p>
      </div>
    </div>
  )
}
