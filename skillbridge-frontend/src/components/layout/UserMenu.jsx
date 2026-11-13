import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { LogOut, User as UserIcon, Settings as SettingsIcon, ChevronDown } from 'lucide-react'
import Avatar from '../ui/Avatar'
import { useAuth } from '../../context/AuthContext'

export default function UserMenu() {
  const { user, logout } = useAuth()
  const nav = useNavigate()
  const [open, setOpen] = useState(false)
  const ref = useRef(null)

  useEffect(() => {
    const h = (e) => ref.current && !ref.current.contains(e.target) && setOpen(false)
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  const go = (path) => {
    setOpen(false)
    nav(path)
  }

  const handleLogout = async () => {
    setOpen(false)
    await logout()
    nav('/login')
  }

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center gap-2 rounded-lg border border-transparent px-1.5 py-1 hover:border-border hover:bg-fg/5 transition-colors"
        aria-haspopup="menu"
        aria-expanded={open}
      >
        <Avatar user={user} size={30} />
        <ChevronDown
          className={`h-3.5 w-3.5 text-muted transition-transform ${open ? 'rotate-180' : ''}`}
        />
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: -8, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.96 }}
            transition={{ duration: 0.15, ease: 'easeOut' }}
            role="menu"
            className="absolute right-0 z-50 mt-2 w-64 overflow-hidden rounded-xl border bg-surface shadow-2xl"
          >
            <div className="border-b bg-gradient-to-br from-primary/10 to-transparent px-4 py-3">
              <p className="truncate text-sm font-semibold">
                {user.firstName} {user.lastName}
              </p>
              <p className="truncate text-xs text-muted">{user.email}</p>
              <div className="mt-2 flex flex-wrap gap-1">
                {user.roles.map((r) => (
                  <span
                    key={r}
                    className="rounded-full bg-primary/15 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-primary"
                  >
                    {r}
                  </span>
                ))}
              </div>
            </div>

            <div className="p-1">
              <button
                role="menuitem"
                onClick={() => go('/profile')}
                className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-fg hover:bg-fg/5 transition-colors"
              >
                <UserIcon className="h-4 w-4 text-muted" />
                My profile
              </button>
              <button
                role="menuitem"
                onClick={() => go('/settings')}
                className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-fg hover:bg-fg/5 transition-colors"
              >
                <SettingsIcon className="h-4 w-4 text-muted" />
                Settings
              </button>
            </div>

            <div className="border-t p-1">
              <button
                role="menuitem"
                onClick={handleLogout}
                className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-danger hover:bg-danger/10 transition-colors"
              >
                <LogOut className="h-4 w-4" />
                Log out
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}