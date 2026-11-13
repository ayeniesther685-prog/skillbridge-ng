import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Spinner from '../components/ui/Spinner'
export default function ProtectedRoute() {
  const { user, restoring } = useAuth()
  const loc = useLocation()
  if (restoring) return <Spinner full />
  if (!user) return <Navigate to={`/login?next=${encodeURIComponent(loc.pathname + loc.search)}`} replace />
  return <Outlet />
}
