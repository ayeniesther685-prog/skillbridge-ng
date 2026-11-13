import { NavLink, Link } from 'react-router-dom'
import { X, GraduationCap } from 'lucide-react'
import { navFor } from './nav'
import { useAuth } from '../../context/AuthContext'

export function NavList({ onNavigate }) {
  const { user } = useAuth()
  return (
    <nav className="space-y-0.5 p-3">
      {navFor(user.roles).map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          onClick={onNavigate}
          end={to.startsWith('/dashboard') || ['/jobs', '/admin/courses', '/profile', '/wallet', '/courses'].includes(to)}
          className={({ isActive }) =>
            `group relative flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-all ${
              isActive
                ? 'bg-primary/10 font-medium text-primary'
                : 'text-muted hover:bg-fg/5 hover:text-fg'
            }`
          }
        >
          {({ isActive }) => (
            <>
              {isActive && (
                <span className="absolute left-0 top-1/2 h-6 w-0.5 -translate-y-1/2 rounded-r bg-primary" />
              )}
              <Icon className={`h-4 w-4 transition-transform ${isActive ? '' : 'group-hover:scale-110'}`} />
              {label}
            </>
          )}
        </NavLink>
      ))}
    </nav>
  )
}

export const Brand = () => (
  <Link to="/" className="flex items-center gap-2.5 px-5 text-base font-semibold tracking-tight">
    <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-accent shadow-lg shadow-primary/20">
      <GraduationCap className="h-4 w-4 text-white" />
    </span>
    <span>SkillBridge NG</span>
  </Link>
)

export default function Sidebar() {
  return (
    <aside className="hidden w-60 shrink-0 border-r bg-surface lg:flex lg:flex-col">
      <div className="flex h-16 items-center border-b">
        <Brand />
      </div>
      <div className="flex-1 overflow-y-auto">
        <NavList />
      </div>
      <div className="border-t p-3">
        <div className="rounded-lg bg-gradient-to-br from-primary/10 to-accent/5 p-3">
          <p className="text-xs font-medium text-fg">SkillBridge NG</p>
          <p className="mt-1 text-[11px] leading-tight text-muted">
            v1.0 · Built for Nigeria
          </p>
        </div>
      </div>
    </aside>
  )
}

export function MobileSidebar({ open, onClose }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 lg:hidden">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <aside className="relative flex h-full w-64 flex-col border-r bg-surface">
        <div className="flex h-16 items-center justify-between border-b pr-3">
          <Brand />
          <button
            onClick={onClose}
            aria-label="Close menu"
            className="rounded-lg p-1.5 hover:bg-fg/5"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto">
          <NavList onNavigate={onClose} />
        </div>
      </aside>
    </div>
  )
}