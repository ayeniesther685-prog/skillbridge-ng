// Minimal placeholder so login has a destination and logout can be tested.
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LogOut } from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import Logo from '../components/common/Logo.jsx'
import LoadingButton from '../components/auth/LoadingButton.jsx'

export default function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [busy, setBusy] = useState(false)

  const handleLogout = async () => {
    setBusy(true)
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-ink-950">
      <header className="flex items-center justify-between border-b border-line px-4 py-4 sm:px-8">
        <Logo />
        <LoadingButton onClick={handleLogout} loading={busy} className="!h-10 !w-auto bg-ink-700 !shadow-none hover:bg-ink-800">
          <LogOut className="h-4 w-4" aria-hidden="true" /> Log out
        </LoadingButton>
      </header>
      <main className="mx-auto max-w-3xl px-4 py-16 text-center">
        <h1 className="text-3xl font-extrabold">Welcome{user?.firstName ? `, ${user.firstName}` : ''}</h1>
        <p className="mt-3 text-muted">Your dashboard is on its way. Authentication is working.</p>
      </main>
    </div>
  )
}
