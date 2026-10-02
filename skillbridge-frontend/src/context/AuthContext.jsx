import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { authService } from '../services/authService.js'
import { tokenStorage } from '../services/api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true) // initial auth check

  const refreshUser = useCallback(async () => {
    const me = await authService.me()
    setUser(me)
    return me
  }, [])

  // Restore session on first load
  useEffect(() => {
    let active = true
    ;(async () => {
      if (!tokenStorage.getAccess() && !tokenStorage.getRefresh()) {
        setLoading(false)
        return
      }
      try {
        const me = await authService.me()
        if (active) setUser(me)
      } catch {
        tokenStorage.clear()
        if (active) setUser(null)
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => { active = false }
  }, [])

  // Refresh failed somewhere in the app -> drop to logged-out
  useEffect(() => {
    const onExpired = () => setUser(null)
    window.addEventListener('auth:session-expired', onExpired)
    return () => window.removeEventListener('auth:session-expired', onExpired)
  }, [])

  const login = useCallback(async (credentials) => {
    const { user: u } = await authService.login(credentials)
    setUser(u ?? (await authService.me()))
  }, [])

  const register = useCallback(async (payload) => {
    const { user: u } = await authService.register(payload)
    setUser(u ?? (await authService.me()))
  }, [])

  const logout = useCallback(async () => {
    try { await authService.logout() } catch { /* clear locally regardless */ }
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, isAuthenticated: !!user, loading, login, register, logout, refreshUser }),
    [user, loading, login, register, logout, refreshUser]
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}
