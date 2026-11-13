import { Link } from 'react-router-dom'
import Button from '../../components/ui/Button'
export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-bg px-4 text-center">
      <p className="font-mono text-sm text-muted">404</p>
      <h1 className="text-2xl font-semibold">Page not found</h1>
      <p className="max-w-sm text-sm text-muted">The page you're looking for doesn't exist or has moved.</p>
      <Button as={Link} to="/">Go home</Button>
    </div>
  )
}
