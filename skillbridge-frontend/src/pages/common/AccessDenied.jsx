import { Link } from 'react-router-dom'
import { ShieldAlert } from 'lucide-react'
import Button from '../../components/ui/Button'
export default function AccessDenied() {
  return (
    <div className="flex flex-col items-center gap-3 py-20 text-center">
      <ShieldAlert className="h-8 w-8 text-warning" />
      <h1 className="text-2xl font-semibold">Access denied</h1>
      <p className="max-w-sm text-sm text-muted">Your account doesn't have the role needed for this page.</p>
      <Button as={Link} to="/dashboard">Back to your dashboard</Button>
    </div>
  )
}
