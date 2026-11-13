import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api/auth'
import { tokens } from '../api/client'

const Ctx = createContext(null)
export const useAuth = () => useContext(Ctx)

export const dashboardFor = (role) => `/dashboard/${role.toLowerCase()}`
const ORDER = ['ADMIN', 'CLIENT', 'FREELANCER', 'STUDENT']
export const primaryRole = (roles = []) => ORDER.find((r) => roles.includes(r)) || 'STUDENT'

export function AuthProvider({ children }) {
  const qc = useQueryClient()
  const [user, setUser] = useState(null)
  const [restoring, setRestoring] = useState(true)

  useEffect(() => {
    let alive = true
    ;(async () => {
      if (!tokens.access && !tokens.refresh) { setRestoring(false); return }
      try {
        const me = await authApi.me() // interceptor refreshes once on 401
        if (alive) setUser(me)
      } catch {
        tokens.clear()
      } finally {
        if (alive) setRestoring(false)
      }
    })()
    return () => { alive = false }
  }, [])

  const acceptSession = useCallback((res) => {
    tokens.set(res.accessToken, res.refreshToken)
    setUser(res.user)
    return res.user
  }, [])

  const login = useCallback(async (body) => acceptSession(await authApi.login(body)), [acceptSession])
  const register = useCallback(async (body) => acceptSession(await authApi.register(body)), [acceptSession])
  const logout = useCallback(() => {
    authApi.logout()
    tokens.clear()
    setUser(null)
    qc.clear()
    window.location.assign('/login')
  }, [qc])

  const hasRole = useCallback((...roles) => !!user?.roles?.some((r) => roles.includes(r)), [user])

  return <Ctx.Provider value={{ user, restoring, login, register, logout, hasRole, isAuthed: !!user }}>{children}</Ctx.Provider>
}
