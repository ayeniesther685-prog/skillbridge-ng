import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL

const ACCESS = 'sb_access_token'
const REFRESH = 'sb_refresh_token'

// "Remember me" => localStorage, otherwise sessionStorage
export const tokenStorage = {
  getAccess: () => localStorage.getItem(ACCESS) ?? sessionStorage.getItem(ACCESS),
  getRefresh: () => localStorage.getItem(REFRESH) ?? sessionStorage.getItem(REFRESH),
  set({ accessToken, refreshToken }, remember) {
    const store = remember === undefined
      ? (localStorage.getItem(REFRESH) ? localStorage : sessionStorage)
      : (remember ? localStorage : sessionStorage)
    const other = store === localStorage ? sessionStorage : localStorage
    other.removeItem(ACCESS); other.removeItem(REFRESH)
    if (accessToken) store.setItem(ACCESS, accessToken)
    if (refreshToken) store.setItem(REFRESH, refreshToken)
  },
  clear() {
    for (const s of [localStorage, sessionStorage]) { s.removeItem(ACCESS); s.removeItem(REFRESH) }
  },
}

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ---- refresh handling (single in-flight refresh, no loops) ----
let refreshPromise = null
const isAuthUrl = (url = '') => /\/api\/auth\/(login|register|refresh|logout)/.test(url)

// Backend AuthResponse: { accessToken, refreshToken, tokenType, expiresInSeconds, user }
export const extractTokens = (data) => ({
  accessToken: data?.accessToken ?? null,
  refreshToken: data?.refreshToken ?? null,
})

async function refreshAccessToken() {
  const refreshToken = tokenStorage.getRefresh()
  if (!refreshToken) throw new Error('No refresh token')
  // plain axios: bypasses our interceptors so a failure can't loop
  const res = await axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken }, { timeout: 15000 })
  const tokens = extractTokens(res.data)
  if (!tokens.accessToken) throw new Error('Refresh response missing access token')
  // Backend returns a NEW token pair on refresh; store both.
  tokenStorage.set({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken ?? refreshToken })
  return tokens.accessToken
}

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config
    const status = error.response?.status
    // Spring Security may answer an expired/invalid JWT with 403 (default entry point), so 401 and 403 both try one refresh.
    if ((status !== 401 && status !== 403) || !original || original._retry || isAuthUrl(original.url) || !tokenStorage.getRefresh()) {
      return Promise.reject(error)
    }
    original._retry = true
    try {
      refreshPromise ??= refreshAccessToken().finally(() => { refreshPromise = null })
      const newToken = await refreshPromise
      original.headers.Authorization = `Bearer ${newToken}`
      return api(original)
    } catch {
      tokenStorage.clear()
      window.dispatchEvent(new Event('auth:session-expired'))
      return Promise.reject(error)
    }
  }
)

export default api
