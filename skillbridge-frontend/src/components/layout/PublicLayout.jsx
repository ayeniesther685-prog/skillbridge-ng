import { Link, NavLink, Outlet } from 'react-router-dom'
import ThemeToggle from './ThemeToggle'
import Button from '../ui/Button'
import { GraduationCap } from 'lucide-react'
import { useAuth, dashboardFor, primaryRole } from '../../context/AuthContext'
const links = [['/jobs', 'Jobs'], ['/courses', 'Courses'], ['/about', 'About'], ['/faq', 'FAQ']]
export default function PublicLayout() {
  const { user } = useAuth()
  return (
    <div className="flex min-h-screen flex-col bg-bg">
      <header className="sticky top-0 z-30 border-b bg-bg/90 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-6xl items-center gap-6 px-4">
         <Link to="/" className="flex items-center gap-2 font-semibold tracking-tight">
  <span className="flex h-6 w-6 items-center justify-center rounded-md bg-primary">
    <GraduationCap className="h-4 w-4 text-white" />
  </span>
  SkillBridge NG
</Link>
          <nav className="hidden gap-5 text-sm sm:flex">
            {links.map(([to, l]) => <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'text-fg' : 'text-muted hover:text-fg')}>{l}</NavLink>)}
          </nav>
          <div className="ml-auto flex items-center gap-2">
            <ThemeToggle />
            {user
              ? <Button as={Link} to={dashboardFor(primaryRole(user.roles))} size="sm">Dashboard</Button>
              : <><Button as={Link} to="/login" variant="ghost" size="sm">Log in</Button><Button as={Link} to="/register" size="sm">Sign up</Button></>}
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8"><Outlet /></main>
      <footer className="border-t py-6 text-center text-sm text-muted">
        © {new Date().getFullYear()} SkillBridge NG · <Link to="/terms" className="hover:text-fg">Terms</Link> · <Link to="/privacy" className="hover:text-fg">Privacy</Link> · <Link to="/contact" className="hover:text-fg">Contact</Link>
      </footer>
    </div>
  )
}
