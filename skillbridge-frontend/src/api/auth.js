import { api, bare, tokens } from './client'

export const authApi = {
  login: (body) => bare.post('/api/auth/login', body).then((r) => r.data),
  register: (body) => bare.post('/api/auth/register', body).then((r) => r.data),
  me: () => api.get('/api/auth/me').then((r) => r.data),
  logout: () => {
    const refreshToken = tokens.refresh
    if (refreshToken) api.post('/api/auth/logout', { refreshToken }).catch(() => {})
  },

  // Phase 3A: email verification
  verifyEmail: (token) =>
    bare.get('/api/auth/verify-email', { params: { token } }).then((r) => r.data),
  resendVerification: (email) =>
    bare.post('/api/auth/resend-verification', { email }).then((r) => r.data),
}
