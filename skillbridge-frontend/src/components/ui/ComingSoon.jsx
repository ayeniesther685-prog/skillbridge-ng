import { Clock } from 'lucide-react'
import Card from './Card'
export default function ComingSoon({ title, feature, description, endpoint }) {
  title = title || feature
  return (
    <Card className="border-dashed p-6">
      <div className="flex items-start gap-3">
        <Clock className="mt-0.5 h-5 w-5 text-warning" />
        <div>
          <h3 className="font-semibold">{title} <span className="ml-1 rounded bg-warning/15 px-1.5 py-0.5 text-xs text-warning">Coming in v2</span></h3>
          <p className="mt-1 text-sm text-muted">{description} {endpoint ? <>Needs <code className="font-mono text-xs">{endpoint}</code>.</> : "This needs a backend endpoint that doesn't exist yet, so it stays off until then."}</p>
        </div>
      </div>
    </Card>
  )
}
