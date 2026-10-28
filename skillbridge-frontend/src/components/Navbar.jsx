import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { Bell, Briefcase, FileText, LayoutDashboard, LogOut, Menu, MessageSquare, Settings, User, Users, X, FileSignature } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import * as notificationApi from '../api/notificationApi.js'
import Logo from './common/Logo.jsx'

const publicLinks = [
  { to: '/jobs', label: 'Jobs', icon: Briefcase },
  { to: '/freelancers', label: 'People', icon: Users },
]
const authLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/jobs', label: 'Jobs', icon: Briefcase },
  { to: '/applications', label: 'Applications', icon: FileText },
  { to: '/contracts', label: 'Contracts', icon: FileSignature },
  { to: '/messages', label: 'Messages', icon: MessageSquare },
]

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [open, setOpen] = useState(false)
  const [unread, setUnread] = useState(0)

  useEffect(() => setOpen(false), [location.pathname])

  useEffect(() => {
    if (!isAuthenticated) return
    let active = true
    const load = () => notificationApi.unreadCount().then((d) => active && setUnread(d.count)).catch(() => {})
    load()
    const t = setInterval(load, 60000)
    return () => { active = false; clearInterval(t) }
  }, [isAuthenticated, location.pathname])

  const handleLogout = async () => { await logout(); navigate('/login', { replace: true }) }
  const links = isAuthenticated ? authLinks : publicLinks
  const linkCls = ({ isActive }) =>
    `flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-semibold transition-colors ${isActive ? 'bg-brand-500/15 text-white' : 'text-slate-300 hover:bg-white/5'}`

  return (
    <header className="sticky top-0 z-40 border-b border-line bg-ink-950/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4 sm:px-6">
        <Link to={isAuthenticated ? '/dashboard' : '/'} aria-label="SkillBridge NG home"><Logo /></Link>

        <nav className="hidden items-center gap-1 lg:flex" aria-label="Main">
          {links.map((l) => <NavLink key={l.to} to={l.to} className={linkCls}><l.icon className="h-4 w-4" aria-hidden="true" />{l.label}</NavLink>)}
        </nav>

        <div className="hidden items-center gap-2 lg:flex">
          {isAuthenticated ? (
            <>
              <Link to="/notifications" aria-label={`Notifications${unread ? `, ${unread} unread` : ''}`} className="relative grid h-10 w-10 place-items-center rounded-xl hover:bg-white/5">
                <Bell className="h-5 w-5" />
                {unread > 0 && <span className="absolute right-1 top-1 grid h-4 min-w-4 place-items-center rounded-full bg-accent px-1 text-[10px] font-bold text-black">{unread > 99 ? '99+' : unread}</span>}
              </Link>
              <Link to="/profile" className="flex items-center gap-2 rounded-xl px-2 py-1.5 hover:bg-white/5">
                <span className="grid h-8 w-8 place-items-center rounded-full bg-brand-500 text-xs font-bold">{user?.firstName?.[0]}{user?.lastName?.[0]}</span>
                <span className="text-sm font-semibold">{user?.firstName}</span>
              </Link>
              <Link to="/settings" aria-label="Settings" className="grid h-10 w-10 place-items-center rounded-xl hover:bg-white/5"><Settings className="h-5 w-5" /></Link>
              <button onClick={handleLogout} aria-label="Log out" className="grid h-10 w-10 place-items-center rounded-xl hover:bg-white/5"><LogOut className="h-5 w-5" /></button>
            </>
          ) : (
            <>
              <Link to="/login" className="rounded-xl px-4 py-2 text-sm font-bold hover:bg-white/5">Sign in</Link>
              <Link to="/register" className="rounded-xl bg-brand-500 px-4 py-2 text-sm font-bold hover:bg-brand-600">Get started</Link>
            </>
          )}
        </div>

        <button className="grid h-10 w-10 place-items-center rounded-xl hover:bg-white/5 lg:hidden" onClick={() => setOpen((o) => !o)} aria-expanded={open} aria-label="Toggle menu">
          {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </div>

      {open && (
        <div className="border-t border-line bg-ink-950 px-4 py-3 lg:hidden">
          <nav className="flex flex-col gap-1" aria-label="Mobile">
            {links.map((l) => <NavLink key={l.to} to={l.to} className={linkCls}><l.icon className="h-4 w-4" />{l.label}</NavLink>)}
            {isAuthenticated ? (
              <>
                <NavLink to="/notifications" className={linkCls}><Bell className="h-4 w-4" />Notifications{unread > 0 && <span className="ml-auto rounded-full bg-accent px-2 text-xs font-bold text-black">{unread}</span>}</NavLink>
                <NavLink to="/profile" className={linkCls}><User className="h-4 w-4" />Profile</NavLink>
                <NavLink to="/settings" className={linkCls}><Settings className="h-4 w-4" />Settings</NavLink>
                <button onClick={handleLogout} className="flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm font-semibold text-red-300 hover:bg-white/5"><LogOut className="h-4 w-4" />Log out</button>
              </>
            ) : (
              <div className="mt-2 grid grid-cols-2 gap-2">
                <Link to="/login" className="rounded-xl border border-line py-2.5 text-center text-sm font-bold">Sign in</Link>
                <Link to="/register" className="rounded-xl bg-brand-500 py-2.5 text-center text-sm font-bold">Get started</Link>
              </div>
            )}
          </nav>
        </div>
      )}
    </header>
  )
}
