import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
export default function RoleGate({ roles }) {
  const { hasRole } = useAuth()
  return hasRole(...roles) ? <Outlet /> : <Navigate to="/access-denied" replace />
}
