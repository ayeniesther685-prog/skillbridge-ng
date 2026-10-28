import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import LoadingSpinner from './LoadingSpinner.jsx'

// Guest-only pages (login/register): signed-in users go to the dashboard.
export default function PublicRoute() {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <LoadingSpinner fullScreen />
  if (isAuthenticated) return <Navigate to="/dashboard" replace />
  return <Outlet />
}
