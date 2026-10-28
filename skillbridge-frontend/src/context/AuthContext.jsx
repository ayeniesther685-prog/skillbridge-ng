import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import * as authApi from '../api/authApi.js'
import { tokenStorage } from '../utils/token.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    const me = await authApi.me()
    setUser(me)
    return me
  }, [])

  // Restore session. A 401 here is transparently refreshed by the axios interceptor.
  useEffect(() => {
    let active = true
    ;(async () => {
      if (!tokenStorage.getAccess() && !tokenStorage.getRefresh()) { setLoading(false); return }
      try {
        const me = await authApi.me()
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

  useEffect(() => {
    const onExpired = () => setUser(null)
    window.addEventListener('auth:session-expired', onExpired)
    return () => window.removeEventListener('auth:session-expired', onExpired)
  }, [])

  const login = useCallback(async ({ email, password, remember = true }) => {
    const data = await authApi.login({ email, password })
    tokenStorage.set(data, remember)
    setUser(data.user)
  }, [])

  const register = useCallback(async (payload) => {
    const data = await authApi.register(payload)
    tokenStorage.set(data, true)
    setUser(data.user)
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = tokenStorage.getRefresh()
    if (refreshToken) authApi.logout(refreshToken).catch(() => {}) // fire-and-forget
    tokenStorage.clear()
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
