import axios from 'axios'

const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'
export const tokens = {
  get access() { return localStorage.getItem('sb_access') },
  get refresh() { return localStorage.getItem('sb_refresh') },
  set(a, r) { localStorage.setItem('sb_access', a); if (r) localStorage.setItem('sb_refresh', r) },
  clear() { localStorage.removeItem('sb_access'); localStorage.removeItem('sb_refresh') },
}

export const api = axios.create({ baseURL: BASE, headers: { 'Content-Type': 'application/json' } })
const bare = axios.create({ baseURL: BASE, headers: { 'Content-Type': 'application/json' } })

api.interceptors.request.use((cfg) => {
  const t = tokens.access
  if (t) cfg.headers.Authorization = `Bearer ${t}`
  return cfg
})

let refreshing = null
function refreshOnce() {
  if (!refreshing) {
    const rt = tokens.refresh
    if (!rt) return Promise.reject(new Error('no refresh token'))
    refreshing = bare.post('/api/auth/refresh', { refreshToken: rt })
      .then((r) => { tokens.set(r.data.accessToken, r.data.refreshToken); return r.data })
      .finally(() => { refreshing = null })
  }
  return refreshing
}

function toLogin() {
  tokens.clear()
  const here = window.location.pathname + window.location.search
  if (!window.location.pathname.startsWith('/login')) {
    window.location.assign(`/login?next=${encodeURIComponent(here)}`)
  }
}

api.interceptors.response.use(
  (r) => r,
  async (error) => {
    const { response, config } = error
    if (!response || !config) return Promise.reject(error)
    const isRefreshCall = config.url?.includes('/api/auth/refresh')
    const isAuthCall = config.url?.includes('/api/auth/login') || config.url?.includes('/api/auth/register')
    if (response.status === 401 && !isRefreshCall && !isAuthCall && !config._retried) {
      config._retried = true
      try {
        await refreshOnce()
        config.headers.Authorization = `Bearer ${tokens.access}`
        return api(config)
      } catch {
        toLogin()
      }
    }
    if (response.status === 403 && tokens.access && !window.location.pathname.startsWith('/access-denied')) {
      // Let callers handle it first; pages may opt out with config.skipForbiddenRedirect
      if (!config.skipForbiddenRedirect) window.location.assign('/access-denied')
    }
    return Promise.reject(error)
  }
)

export { bare }
