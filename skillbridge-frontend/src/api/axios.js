import axios from 'axios'
import { tokenStorage } from '../utils/token.js'

const BASE_URL = import.meta.env.VITE_API_BASE_URL

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
  paramsSerializer: { indexes: null }, // skill=1&skill=2
})

export const unwrap = (promise) => promise.then((r) => r.data)

api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let refreshPromise = null
const isAuthUrl = (url = '') => /\/api\/auth\/(login|register|refresh|logout)/.test(url)

async function refreshTokens() {
  const refreshToken = tokenStorage.getRefresh()
  if (!refreshToken) throw new Error('No refresh token')
  // plain axios: bypasses these interceptors, so refresh itself is never retried
  const { data } = await axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken }, { timeout: 15000 })
  tokenStorage.set(data) // refresh tokens rotate: store the NEW pair
  return data.accessToken
}

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config
    if (error.response?.status !== 401 || !original || original._retry || isAuthUrl(original.url) || !tokenStorage.getRefresh()) {
      return Promise.reject(error)
    }
    original._retry = true // request-scoped: one retry only
    try {
      refreshPromise ??= refreshTokens().finally(() => { refreshPromise = null })
      const token = await refreshPromise
      original.headers.Authorization = `Bearer ${token}`
      return api(original)
    } catch {
      tokenStorage.clear()
      window.dispatchEvent(new Event('auth:session-expired'))
      return Promise.reject(error)
    }
  }
)

/** CRUD helper for /api/users/{id}/<name> (read), /api/users/me/<name> (create), /api/<name>/{id} (update/delete) */
export const makeResourceApi = (name) => ({
  list: (userId) => unwrap(api.get(`/api/users/${userId}/${name}`)),
  create: (body) => unwrap(api.post(`/api/users/me/${name}`, body)),
  update: (id, body) => unwrap(api.put(`/api/${name}/${id}`, body)),
  remove: (id) => unwrap(api.delete(`/api/${name}/${id}`)),
})

export default api
