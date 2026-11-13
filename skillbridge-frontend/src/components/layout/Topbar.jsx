import { Menu } from 'lucide-react'
import { useNavigate, useLocation } from 'react-router-dom'
import ThemeToggle from './ThemeToggle'
import NotificationBell from './NotificationBell'
import UserMenu from './UserMenu'
import { useAuth } from '../../context/AuthContext'
export default function Topbar({ onMenu }) {
  const { user } = useAuth()
  const nav = useNavigate()
  const { pathname } = useLocation()
  const current = pathname.startsWith('/dashboard/') ? pathname.split('/')[2].toUpperCase() : ''
  return (
    <header className="sticky top-0 z-30 flex h-14 items-center gap-3 border-b bg-bg/90 px-4 backdrop-blur">
      <button className="rounded p-2 hover:bg-fg/5 lg:hidden" onClick={onMenu} aria-label="Open menu"><Menu className="h-5 w-5" /></button>
      {user.roles.length > 1 && (
        <select aria-label="Switch dashboard" value={user.roles.includes(current) ? current : ''} onChange={(e) => e.target.value && nav(`/dashboard/${e.target.value.toLowerCase()}`)}
          className="field !w-auto !py-1.5">
          <option value="" disabled>Switch dashboard</option>
          {user.roles.map((r) => <option key={r} value={r}>{r[0] + r.slice(1).toLowerCase()}</option>)}
        </select>
      )}
      <div className="ml-auto flex items-center gap-1"><ThemeToggle /><NotificationBell /><UserMenu /></div>
    </header>
  )
}
