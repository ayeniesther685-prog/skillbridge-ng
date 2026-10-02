import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import FullScreenLoader from '../components/common/FullScreenLoader.jsx'

export default function GuestRoute() {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <FullScreenLoader />
  if (isAuthenticated) return <Navigate to="/dashboard" replace />
  return <Outlet />
}
